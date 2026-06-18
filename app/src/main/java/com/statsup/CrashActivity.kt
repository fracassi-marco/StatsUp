package com.statsup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.statsup.ui.components.CrashScreen

class CrashActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CRASH_MESSAGE = "crash_message"
        const val EXTRA_STACK_TRACE = "stack_trace"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val message = intent.getStringExtra(EXTRA_CRASH_MESSAGE) ?: "Unknown error"
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: ""
        setContent {
            val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colorScheme) {
                CrashScreen(
                    message = message,
                    stackTrace = stackTrace,
                    onClose = { finishAffinity() }
                )
            }
        }
    }
}



