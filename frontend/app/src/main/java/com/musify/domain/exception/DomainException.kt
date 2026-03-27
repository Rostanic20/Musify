package com.musify.domain.exception

/**
 * Base exception for domain layer
 */
sealed class DomainException(message: String) : Exception(message)

// Authentication exceptions
class InvalidCredentialsException : DomainException("Invalid username or password")
class UserNotFoundException : DomainException("User not found")
class EmailNotVerifiedException : DomainException("Email not verified")
class TwoFactorRequiredException : DomainException("Two-factor authentication required")
class InvalidTwoFactorCodeException : DomainException("Invalid 2FA code")
class SessionExpiredException : DomainException("Session has expired")

// Validation exceptions
class ValidationException(message: String) : DomainException(message)
class EmailAlreadyExistsException : DomainException("Email already registered")
class UsernameAlreadyExistsException : DomainException("Username already taken")

// Network exceptions
class NetworkException(message: String = "Network error occurred") : DomainException(message)
class ServerException(message: String = "Server error occurred") : DomainException(message)
class TimeoutException : DomainException("Request timed out")

// Business logic exceptions
class SubscriptionRequiredException : DomainException("Premium subscription required")
class PlaylistNotFoundException : DomainException("Playlist not found")
class SongNotFoundException : DomainException("Song not found")
class UnauthorizedException : DomainException("Unauthorized access")
class SongAlreadyInPlaylistException : DomainException("Song already exists in playlist")