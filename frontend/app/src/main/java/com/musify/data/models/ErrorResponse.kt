package com.musify.data.models

/**
 * Generic error response from API
 */
data class ErrorResponse(
    val error: String,
    val message: String? = null,
    val code: String? = null
)