package com.jawa.dto

import com.jawa.entities.Users
import com.jawa.enums.Role
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class User(
    val username: String,
    val name: String,
    val role: Role
)

@Serializable
data class UserRequest(
    val username: String,
    val name: String,
    val originalPassword: String,
    val role: Role
)

fun ResultRow.toUser(): User {
    return User(
        username = this[Users.username],
        name = this[Users.name],
        role = this[Users.role]
    )
}