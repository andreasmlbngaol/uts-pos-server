package com.jawa.auth

import com.jawa.enums.Role
import kotlinx.serialization.Serializable

const val SESSION_TIMEOUT_IN_SECONDS = 30L

@Serializable
data class UserSession(
    val userId: Long,
    val role: Role,
    val token: String,
    val expiredAt: Long = System.currentTimeMillis() + SESSION_TIMEOUT_IN_SECONDS * 1000L
)