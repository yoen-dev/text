package com.nourishwell.models

import kotlinx.serialization.Serializable

// ── User roles ────────────────────────────────────────────────────────────────
enum class UserRole { subscriber, professional }

// ── Stored user (in-memory for now, swap with DB later) ───────────────────────
data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val licenceNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val backdoor: Boolean = false
)

// ── Request bodies ────────────────────────────────────────────────────────────
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String = "subscriber",
    val licenceNumber: String? = null
)

// ── Response bodies ───────────────────────────────────────────────────────────
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
    val redirectTo: String
)

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class BackdoorAccount(
    val email: String,
    val password: String,
    val role: String,
    val redirectTo: String
)

@Serializable
data class BackdoorInfoResponse(
    val note: String,
    val accounts: List<BackdoorAccount>
)
