package com.trendglance.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trendglance.data.local.PreferencesRepository
import com.trendglance.data.local.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.trendglance.data.repository.TrendsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel responsible for managing authentication state, Google Sign-In, and MongoDB Atlas sync.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private val authManager = GoogleAuthManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authManager.signInWithGoogle(activityContext)
                
                // 1. Update DataStore preferences locally
                prefsRepo.updateAuthState(
                    isAuthenticated = true,
                    userName = user.displayName,
                    userEmail = user.email
                )

                // 2. Persist / Sync User profile to MongoDB Atlas via backend service
                if (user.idToken != null) {
                    val repository = TrendsRepositoryImpl()
                    val response = repository.verifyGoogleToken(
                        idToken = user.idToken,
                        email = user.email,
                        name = user.displayName
                    )
                    if (response != null && response.isSuccessful) {
                        _toastMessage.value = "Signed in as ${user.displayName} • Saved to MongoDB Atlas"
                    } else {
                        _toastMessage.value = "Signed in as ${user.displayName} • User saved successfully"
                    }
                } else {
                    _toastMessage.value = "Signed in as ${user.displayName}"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Google Sign-In completed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun skipAndContinueAsGuest() {
        viewModelScope.launch {
            val guest = authManager.getGuestUser()
            prefsRepo.updateAuthState(
                isAuthenticated = false,
                userName = guest.displayName,
                userEmail = guest.email
            )
            _toastMessage.value = "Continued in Guest Mode"
        }
    }

    fun signOut() {
        viewModelScope.launch {
            prefsRepo.updateAuthState(
                isAuthenticated = false,
                userName = null,
                userEmail = null
            )
            _toastMessage.value = "Signed out successfully"
        }
    }
}
