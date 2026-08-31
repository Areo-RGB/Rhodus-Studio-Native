package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PlayerPreset(
    val id: String,
    val name: String,
    val description: String = "",
    val imageResId: Int = 0,
    val imageAssetPath: String = "",
    val leftFootIntervalMs: Long,
    val bothFeetIntervalMs: Long,
    val rightFootIntervalMs: Long,
    val targetFlashes: Int = 15,
    val countdownSeconds: Int = 5,
    val shuffleEnabled: Boolean = false
)

val PLAYER_PRESETS = listOf(
    PlayerPreset(
        id = "bent",
        name = "Bent",
        description = "",
        imageResId = R.drawable.bent,
        imageAssetPath = "avatars/bent.png",
        leftFootIntervalMs = 1400L,
        bothFeetIntervalMs = 1300L,
        rightFootIntervalMs = 1200L,
        targetFlashes = 15,
        countdownSeconds = 5,
        shuffleEnabled = false
    ),
    PlayerPreset(
        id = "jakob",
        name = "Jakob",
        description = "",
        imageResId = R.drawable.jakob,
        imageAssetPath = "avatars/jakob.png",
        leftFootIntervalMs = 1250L,
        bothFeetIntervalMs = 1150L,
        rightFootIntervalMs = 1050L,
        targetFlashes = 15,
        countdownSeconds = 5,
        shuffleEnabled = false
    ),
    PlayerPreset(
        id = "lionel",
        name = "Lionel",
        description = "",
        imageResId = R.drawable.lionel,
        imageAssetPath = "avatars/lionel.png",
        leftFootIntervalMs = 1100L,
        bothFeetIntervalMs = 1000L,
        rightFootIntervalMs = 900L,
        targetFlashes = 15,
        countdownSeconds = 5,
        shuffleEnabled = false
    ),
    PlayerPreset(
        id = "lion",
        name = "Lion",
        description = "",
        imageResId = R.drawable.lion,
        imageAssetPath = "avatars/lion.png",
        leftFootIntervalMs = 900L,
        bothFeetIntervalMs = 800L,
        rightFootIntervalMs = 700L,
        targetFlashes = 15,
        countdownSeconds = 5,
        shuffleEnabled = false
    )
)

enum class AppState {
    SETTINGS, COUNTDOWN, PLAYING, FINISHED
}

data class RhodusUiState(
    val appState: AppState = AppState.SETTINGS,
    val intervalMs: Long = 1000L,
    val targetFlashes: Int = 15,
    val countdownSeconds: Int = 5,
    val shuffleEnabled: Boolean = false,
    val noGridEnabled: Boolean = false,
    
    val currentCountdown: Int = 5,
    val activeCellIndex: Int = -1,
    val changeCount: Int = 0,
    val gridColors: List<Long> = listOf(0xFFFACC15, 0xFFEF4444, 0xFF3B82F6, 0xFF22C55E), // Yellow, Red, Blue, Green
    
    val isFullscreen: Boolean = false
)

class RhodusViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RhodusUiState())
    val uiState: StateFlow<RhodusUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var flashJob: Job? = null
    
    private val defaultColors = listOf(0xFFFACC15, 0xFFEF4444, 0xFF3B82F6, 0xFF22C55E)
    
    fun updateInterval(ms: Long) {
        _uiState.update { it.copy(intervalMs = ms.coerceIn(50, 5000)) }
    }
    
    fun updateTargetFlashes(count: Int) {
        _uiState.update { it.copy(targetFlashes = count.coerceIn(1, 1000)) }
    }
    
    fun updateCountdown(seconds: Int) {
        _uiState.update { it.copy(countdownSeconds = seconds.coerceIn(1, 60)) }
    }
    
    fun toggleShuffle() {
        _uiState.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
    }
    
    fun toggleNoGrid() {
        _uiState.update { it.copy(noGridEnabled = !it.noGridEnabled) }
    }
    
    fun selectPlayerPreset(preset: PlayerPreset, intervalMs: Long = preset.bothFeetIntervalMs) {
        _uiState.update { 
            it.copy(
                intervalMs = intervalMs,
                targetFlashes = preset.targetFlashes,
                shuffleEnabled = preset.shuffleEnabled,
                countdownSeconds = preset.countdownSeconds
            ) 
        }
        startCountdown()
    }
    
    fun startCountdown() {
        countdownJob?.cancel()
        flashJob?.cancel()
        
        var newColors = defaultColors.shuffled()
        if (newColors == _uiState.value.gridColors && defaultColors.size > 1) {
            newColors = defaultColors.shuffled()
        }
        
        _uiState.update { 
            it.copy(
                appState = AppState.COUNTDOWN, 
                currentCountdown = it.countdownSeconds,
                changeCount = 0,
                activeCellIndex = 3, // Start at lower right
                gridColors = newColors
            ) 
        }
        
        countdownJob = viewModelScope.launch {
            if (_uiState.value.shuffleEnabled) {
                // Shuffle animation
                val shuffleDuration = 2000L
                val shuffleStep = 80L
                val steps = (shuffleDuration / shuffleStep).toInt()
                
                var litCellIndex = -1
                for (i in 0 until steps) {
                    val colors = defaultColors.shuffled()
                    val offset = 1 + Random.nextInt(colors.size - 1)
                    litCellIndex = (litCellIndex + offset + colors.size) % colors.size
                    _uiState.update { it.copy(gridColors = colors, activeCellIndex = litCellIndex) }
                    delay(shuffleStep)
                }
                
                var finalColors: List<Long>
                do {
                    finalColors = defaultColors.shuffled()
                } while(finalColors == defaultColors)
                
                _uiState.update { it.copy(gridColors = finalColors, activeCellIndex = 3) }
            }
            
            // Countdown
            var remaining = _uiState.value.countdownSeconds
            while (remaining > 0) {
                _uiState.update { it.copy(currentCountdown = remaining) }
                delay(1000)
                remaining--
            }
            startFlashing()
        }
    }
    
    private fun startFlashing() {
        _uiState.update { 
            it.copy(
                appState = AppState.PLAYING,
                changeCount = 1,
                activeCellIndex = Random.nextInt(4)
            ) 
        }
        
        flashJob = viewModelScope.launch {
            while (true) {
                delay(_uiState.value.intervalMs)
                
                val current = _uiState.value
                if (current.changeCount >= current.targetFlashes) {
                    _uiState.update { it.copy(appState = AppState.FINISHED, activeCellIndex = -1) }
                    break
                }
                
                val offset = 1 + Random.nextInt(3)
                val nextIndex = (current.activeCellIndex + offset) % 4
                
                _uiState.update { 
                    it.copy(
                        changeCount = it.changeCount + 1,
                        activeCellIndex = nextIndex
                    ) 
                }
            }
        }
    }
    
    fun stopTraining() {
        countdownJob?.cancel()
        flashJob?.cancel()
        _uiState.update { 
            it.copy(
                appState = AppState.SETTINGS,
                activeCellIndex = -1,
                gridColors = defaultColors
            ) 
        }
    }
    
    fun showSettings() {
        _uiState.update { it.copy(appState = AppState.SETTINGS) }
    }
    
    fun getContrastColor(backgroundColor: Long): Long {
        val r = (backgroundColor shr 16 and 0xFF).toInt()
        val g = (backgroundColor shr 8 and 0xFF).toInt()
        val b = (backgroundColor and 0xFF).toInt()
        val brightness = (r * 299 + g * 587 + b * 114) / 1000
        return if (brightness > 130) 0xFF000000 else 0xFFFFFFFF
    }
}
