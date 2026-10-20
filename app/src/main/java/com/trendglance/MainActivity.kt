package com.trendglance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.trendglance.ui.auth.AuthViewModel
import com.trendglance.ui.auth.WelcomeScreen
import com.trendglance.ui.dashboard.DashboardScreen
import com.trendglance.ui.dashboard.DashboardViewModel
import com.trendglance.ui.theme.TrendGlanceTheme

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect

/**
 * Main Activity hosting the Auth Welcome Screen and In-App Configuration Dashboard.
 */
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrendGlanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userPrefs by authViewModel.userPreferences.collectAsState()
                    val isLoading by authViewModel.isLoading.collectAsState()
                    val toastMsg by authViewModel.toastMessage.collectAsState()

                    LaunchedEffect(toastMsg) {
                        toastMsg?.let { msg ->
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            authViewModel.clearToastMessage()
                        }
                    }

                    if (userPrefs.isAuthenticated || userPrefs.userName != null) {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onSignOut = { authViewModel.signOut() }
                        )
                    } else {
                        WelcomeScreen(
                            isLoading = isLoading,
                            onSignInWithGoogle = { context -> authViewModel.signInWithGoogle(context) },
                            onSkipAsGuest = { authViewModel.skipAndContinueAsGuest() }
                        )
                    }
                }
            }
        }
    }
}
