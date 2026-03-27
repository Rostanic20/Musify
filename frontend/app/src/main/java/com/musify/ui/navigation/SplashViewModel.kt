package com.musify.ui.navigation

import androidx.lifecycle.ViewModel
import com.musify.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val tokenManager: TokenManager
) : ViewModel() {
    
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}