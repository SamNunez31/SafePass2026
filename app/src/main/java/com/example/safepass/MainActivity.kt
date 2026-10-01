package com.example.safepass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.safepass.ui.SafePassScreen
import com.example.safepass.ui.theme.SafePass2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafePass2026Theme {
                SafePassScreen()
            }
        }
    }
}