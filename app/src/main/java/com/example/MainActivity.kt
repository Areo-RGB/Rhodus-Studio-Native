package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.MyApplicationTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashFile = File(filesDir, "crash.txt")

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            crashFile.writeText(throwable.stackTraceToString())
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        applyImmersiveMode()

        if (crashFile.exists()) {
            val crashText = crashFile.readText()
            crashFile.delete()
            setContent {
                MyApplicationTheme {
                    Text(
                        text = "CRASH LOG:\n$crashText",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
            return
        }

        try {
            setContent {
                MyApplicationTheme {
                    RhodusScreen()
                }
            }
        } catch (e: Throwable) {
            val crashText = e.stackTraceToString()
            setContent {
                MyApplicationTheme {
                    Text(
                        text = "SETCONTENT CRASH:\n$crashText",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
