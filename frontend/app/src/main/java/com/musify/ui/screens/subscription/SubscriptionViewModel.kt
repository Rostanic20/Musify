package com.musify.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musify.data.api.MusifyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionState(
    val isLoading: Boolean = false,
    val isPremium: Boolean = false,
    val selectedPlan: SubscriptionPlan = SubscriptionPlan.MONTHLY,
    val isSubscribing: Boolean = false,
    val subscribeSuccess: Boolean = false,
    val errorMessage: String? = null
)

enum class SubscriptionPlan(val label: String, val price: String, val period: String) {
    MONTHLY("Monthly", "$9.99", "/month"),
    YEARLY("Yearly", "$99.99", "/year")
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val apiService: MusifyApiService
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init {
        loadCurrentSubscription()
    }

    private fun loadCurrentSubscription() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val userResponse = apiService.getCurrentUser()
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isPremium = user?.isPremium == true
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Network error. Please try again.")
            }
        }
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _state.value = _state.value.copy(selectedPlan = plan)
    }

    fun subscribe() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubscribing = true, errorMessage = null)

            try {
                val request = mapOf("plan" to _state.value.selectedPlan.name.lowercase())
                val response = apiService.subscribe(request)

                if (response.isSuccessful) {
                    _state.value = _state.value.copy(
                        isSubscribing = false,
                        subscribeSuccess = true,
                        isPremium = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isSubscribing = false,
                        errorMessage = "Subscription failed. Please try again."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSubscribing = false, errorMessage = "Network error. Please try again.")
            }
        }
    }
}
