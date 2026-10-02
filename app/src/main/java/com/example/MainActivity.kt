package com.example

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DocVaultViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DocVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light "paper" theme -> dark status/navigation bar icons, always
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val prefs = getSharedPreferences("visor_prefs", Context.MODE_PRIVATE)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // First launch only: show the guide, then remember it was seen
                    var showGuide by remember { mutableStateOf(!prefs.getBoolean("guide_done", false)) }
                    if (showGuide) {
                        OnboardingScreen(onFinish = {
                            prefs.edit().putBoolean("guide_done", true).apply()
                            showGuide = false
                        })
                    } else {
                        MainAppScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
