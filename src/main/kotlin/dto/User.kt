package com.jawa.dto

import com.jawa.entities.Users
import com.jawa.enums.Role
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class User(
    val id: Long,
    val username: String,
    val name: String,
    val role: Role
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
    val newPassword: String? = null
)

fun ResultRow.toUser(): User {
    return User(
        id = this[Users.id].value,
        username = this[Users.username],
        name = this[Users.name],
        role = this[Users.role]
    )
}