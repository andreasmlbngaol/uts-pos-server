package com.jawa.dto

import com.jawa.enums.Role
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val username: String,
    val name: String,
    val role: Role,
    val mustChangePassword: Boolean
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val name: String,
    val role: Role
)

@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val name: String? = null,
    val resetPassword: Boolean? = null,
    val newPassword: String? = null,
    val oldPassword: String? = null
)