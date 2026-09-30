package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.roundtable.RoundtableScreen
import com.example.ui.theme.OculusBackground
import com.example.ui.theme.OculusRoundtableTheme
import com.example.ui.theme.PersonaTeal
import com.example.ui.viewmodel.RoundtableViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OculusRoundtableTheme {
                val viewModel: RoundtableViewModel = viewModel()
                val preferences by viewModel.userPreferences.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OculusBackground)
                        .safeDrawingPadding()
                ) {
                    when {
                        preferences == null -> {
                            // Loading state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = com.example.ui.theme.MatrixGreen)
                            }
                        }
                        preferences?.isFirstLaunch == true -> {
                            // First Launch: Onboarding Diagnostic Bot
                            OnboardingScreen(
                                onComplete = { domain, format, rigor, temp, rosterIds ->
                                    viewModel.completeOnboarding(domain, format, rigor, temp, rosterIds)
                                }
                            )
                        }
                        else -> {
                            // Main Oculus Roundtable Interface
                            RoundtableScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
