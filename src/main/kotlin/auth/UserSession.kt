package com.jawa.auth

import com.jawa.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val userId: Long, val role: Role)