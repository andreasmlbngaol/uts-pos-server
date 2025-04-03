package com.jawa.entities

import com.jawa.enums.Role
import org.jetbrains.exposed.dao.id.LongIdTable

object Users: LongIdTable("users") {
    val username = varchar("username", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName<Role>("role", 255)
    val mustChangePassword = bool("must_change_password")
}