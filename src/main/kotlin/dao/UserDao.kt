package com.jawa.dao

import com.jawa.dto.UserRequest
import com.jawa.dto.toUser
import com.jawa.entities.Users
import com.jawa.service.hashed
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserDao {
    suspend fun getAllUsers() = newSuspendedTransaction(Dispatchers.IO) {
        Users
            .selectAll()
            .orderBy(Users.id to SortOrder.ASC)
            .map { it.toUser() }
    }

    suspend fun insertUser(request: UserRequest) = newSuspendedTransaction(Dispatchers.IO) {
        Users
            .insert {
                it[username] = request.username
                it[name] = request.name
                it[passwordHash] = request.originalPassword.hashed()
                it[role] = request.role
            }
    }

    suspend fun getUserByUsername(username: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users
            .selectAll()
            .where { Users.username eq username }
            .singleOrNull()
    }
}