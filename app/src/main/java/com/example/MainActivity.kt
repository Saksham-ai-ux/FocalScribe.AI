package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.ScriptViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ScriptViewModel = viewModel()
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    // Crossfade animation between screens for high-fidelity transitions
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Welcome -> {
                                WelcomeScreen(
                                    viewModel = viewModel,
                                    onGetStarted = { viewModel.navigateTo(Screen.Dashboard) }
                                )
                            }
                            is Screen.Dashboard -> {
                                DashboardScreen(
                                    viewModel = viewModel
                                )
                            }
                            is Screen.Creator -> {
                                CreatorScreen(
                                    viewModel = viewModel
                                )
                            }
                            is Screen.Preview -> {
                                PreviewScreen(
                                    viewModel = viewModel,
                                    scriptId = screen.scriptId,
                                    isNew = screen.isNew
                                )
                            }
                            is Screen.Teleprompter -> {
                                TeleprompterScreen(
                                    viewModel = viewModel,
                                    scriptId = screen.scriptId
                                )
                            }
                            is Screen.Paywall -> {
                                PaywallScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
