package com.example

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RhodusScreen(viewModel: RhodusViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(enabled = uiState.appState == AppState.PLAYING || uiState.appState == AppState.COUNTDOWN) {
                viewModel.stopTraining()
            }
    ) {
        if (uiState.noGridEnabled) {
            val activeIndex = uiState.activeCellIndex
            val activeColor = if (activeIndex in 0..3) Color(uiState.gridColors[activeIndex]) else Color.Black
            val textColor = if (activeIndex in 0..3) Color(viewModel.getContrastColor(uiState.gridColors[activeIndex])) else Color.White

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top 2/3: Active color display
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .background(activeColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.appState == AppState.COUNTDOWN) {
                        Text(
                            text = "${uiState.currentCountdown}",
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("countdown_text")
                        )
                    } else if (activeIndex in 0..3 && uiState.changeCount > 0) {
                        Text(
                            text = "${uiState.changeCount}",
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                    }
                }

                // Bottom 1/3: Static color layout grid (all 4 colors fully lit up)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(uiState.gridColors[0]))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(uiState.gridColors[1]))
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(uiState.gridColors[2]))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(uiState.gridColors[3]))
                        )
                    }
                }
            }
        } else {
            // Grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    ColorCell(
                        color = Color(uiState.gridColors[0]),
                        isActive = uiState.activeCellIndex == 0,
                        changeCount = uiState.changeCount,
                        textColor = Color(viewModel.getContrastColor(uiState.gridColors[0])),
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                    )
                    ColorCell(
                        color = Color(uiState.gridColors[1]),
                        isActive = uiState.activeCellIndex == 1,
                        changeCount = uiState.changeCount,
                        textColor = Color(viewModel.getContrastColor(uiState.gridColors[1])),
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                    )
                }
                Row(modifier = Modifier.weight(1f)) {
                    ColorCell(
                        color = Color(uiState.gridColors[2]),
                        isActive = uiState.activeCellIndex == 2,
                        changeCount = uiState.changeCount,
                        textColor = Color(viewModel.getContrastColor(uiState.gridColors[2])),
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                    )
                    ColorCell(
                        color = Color(uiState.gridColors[3]),
                        isActive = uiState.activeCellIndex == 3,
                        changeCount = uiState.changeCount,
                        textColor = Color(viewModel.getContrastColor(uiState.gridColors[3])),
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                    )
                }
            }
        }

        // Overlays
        AnimatedVisibility(
            visible = uiState.appState == AppState.COUNTDOWN && !uiState.noGridEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "${uiState.currentCountdown}",
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.testTag("countdown_text")
            )
        }

        AnimatedVisibility(
            visible = uiState.appState == AppState.FINISHED,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { viewModel.showSettings() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(0.8f)
                        .clickable(enabled = false) {}, // absorb clicks
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ziel erreicht",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.changeCount} Farbwechsel abgeschlossen",
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.startCountdown() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restart_button")
                        ) {
                            Text("Erneut starten", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.showSettings() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_button")
                        ) {
                            Text("Einstellungen")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.appState == AppState.SETTINGS,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                SettingsPanel(
                    uiState = uiState,
                    onStart = { viewModel.startCountdown() },
                    onPlayerSelect = { preset, interval -> viewModel.selectPlayerPreset(preset, interval) },
                    onIntervalChange = { viewModel.updateInterval(it) },
                    onTargetFlashesChange = { viewModel.updateTargetFlashes(it) },
                    onCountdownChange = { viewModel.updateCountdown(it) },
                    onToggleNoGrid = { viewModel.toggleNoGrid() }
                )
            }
        }
    }
}

@Composable
fun ColorCell(
    color: Color,
    isActive: Boolean,
    changeCount: Int,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (!isActive) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
        } else if (changeCount > 0) {
            Text(
                text = "$changeCount",
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

@Composable
fun SettingsPanel(
    uiState: RhodusUiState,
    onStart: () -> Unit,
    onPlayerSelect: (PlayerPreset, Long) -> Unit,
    onIntervalChange: (Long) -> Unit,
    onTargetFlashesChange: (Int) -> Unit,
    onCountdownChange: (Int) -> Unit,
    onToggleNoGrid: () -> Unit
) {
    var activeTab by remember { mutableStateOf("players") }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.9f),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hic Rhodus",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                TabButton(text = "PLAYERS", isActive = activeTab == "players", onClick = { activeTab = "players" }, modifier = Modifier.weight(1f))
                TabButton(text = "SETUP", isActive = activeTab == "setup", onClick = { activeTab = "setup" }, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == "players") {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    PLAYER_PRESETS.forEach { preset ->
                        PlayerCard(preset = preset, onSelect = onPlayerSelect)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    SettingStepper(
                        label = "Intervall",
                        value = "${uiState.intervalMs}ms",
                        onMinus = { onIntervalChange(uiState.intervalMs - 100) },
                        onPlus = { onIntervalChange(uiState.intervalMs + 100) }
                    )
                    SettingStepper(
                        label = "Farbwechsel Anzahl",
                        value = "${uiState.targetFlashes}",
                        onMinus = { onTargetFlashesChange(uiState.targetFlashes - 1) },
                        onPlus = { onTargetFlashesChange(uiState.targetFlashes + 1) }
                    )
                    SettingStepper(
                        label = "Countdown",
                        value = "${uiState.countdownSeconds}s",
                        onMinus = { onCountdownChange(uiState.countdownSeconds - 1) },
                        onPlus = { onCountdownChange(uiState.countdownSeconds + 1) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("NoGrid", color = Color.LightGray)
                        Switch(
                            checked = uiState.noGridEnabled,
                            onCheckedChange = { onToggleNoGrid() }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_button")
                    ) {
                        Text("Start", color = Color.White, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) Color.Black else Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PlayerCard(preset: PlayerPreset, onSelect: (PlayerPreset, Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var leftInterval by remember(preset) { mutableStateOf(preset.leftFootIntervalMs) }
    var bothInterval by remember(preset) { mutableStateOf(preset.bothFeetIntervalMs) }
    var rightInterval by remember(preset) { mutableStateOf(preset.rightFootIntervalMs) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    val avatarBitmap = remember(preset.imageAssetPath, preset.imageResId) {
                        if (preset.imageAssetPath.isNotEmpty()) {
                            try {
                                context.assets.open(preset.imageAssetPath).use { inputStream ->
                                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                                }
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                    }

                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = preset.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (preset.imageResId != 0) {
                        Image(
                            painter = painterResource(id = preset.imageResId),
                            contentDescription = preset.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (preset.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = preset.description,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = if (expanded) "▲" else "▼",
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121212))
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFF2A2A2A), modifier = Modifier.padding(bottom = 12.dp))

                    FootIntervalRow(
                        label = "Linker Fuß",
                        intervalMs = leftInterval,
                        onMinus = { leftInterval = (leftInterval - 50).coerceAtLeast(100) },
                        onPlus = { leftInterval = (leftInterval + 50).coerceAtMost(5000) },
                        onStart = { onSelect(preset, leftInterval) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FootIntervalRow(
                        label = "Beide Füße",
                        intervalMs = bothInterval,
                        onMinus = { bothInterval = (bothInterval - 50).coerceAtLeast(100) },
                        onPlus = { bothInterval = (bothInterval + 50).coerceAtMost(5000) },
                        onStart = { onSelect(preset, bothInterval) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FootIntervalRow(
                        label = "Rechter Fuß",
                        intervalMs = rightInterval,
                        onMinus = { rightInterval = (rightInterval - 50).coerceAtLeast(100) },
                        onPlus = { rightInterval = (rightInterval + 50).coerceAtMost(5000) },
                        onStart = { onSelect(preset, rightInterval) }
                    )
                }
            }
        }
    }
}

@Composable
fun FootIntervalRow(
    label: String,
    intervalMs: Long,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onStart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("${intervalMs}ms", color = Color(0xFF3B82F6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onMinus,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
            ) {
                Text("-", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onPlus,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Start", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingStepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.LightGray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus, modifier = Modifier.size(36.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))) {
                Text("-", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(value, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp).width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = onPlus, modifier = Modifier.size(36.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
