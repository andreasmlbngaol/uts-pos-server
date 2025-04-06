package com.jawa.entities

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object UserSessions: LongIdTable("user_sessions") {
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val token = varchar("token", 255).uniqueIndex()
    val expiredAt = long("expired_at")
}