package com.jawa.dao

import com.jawa.dto.CreateUserRequest
import com.jawa.dto.toUser
import com.jawa.entities.Users
import com.jawa.service.hashed
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

object UserDao {
    suspend fun getAllUsers() = newSuspendedTransaction(Dispatchers.IO) {
        Users
            .selectAll()
            .orderBy(Users.id to SortOrder.ASC)
            .map { it.toUser() }
    }

    suspend fun insertUser(request: CreateUserRequest, otp: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users.insert {
            it[username] = request.username
            it[name] = request.name
            it[passwordHash] = otp.hashed()
            it[role] = request.role
            it[mustChangePassword] = true
        }
    }

    suspend fun getUserByUsername(username: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users
            .selectAll()
            .where { Users.username eq username }
            .singleOrNull()
    }

    suspend fun getUserById(id: Long) = newSuspendedTransaction(Dispatchers.IO) {
        Users
            .selectAll()
            .where { Users.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun updateUsername(id: Long, newUsername: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users.update(
            where = { Users.id eq id },
            body = { it[username] = newUsername }
        )
    }

    suspend fun updateName(id: Long, newName: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users.update(
            where = { Users.id eq id },
            body = { it[name] = newName }
        )
    }

    suspend fun resetPassword(id: Long, otp: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users.update(
            where = { Users.id eq id },
            body = {
                it[passwordHash] = otp.hashed()
                it[mustChangePassword] = true
            }
        )
    }

    suspend fun updatePassword(id: Long, newPassword: String) = newSuspendedTransaction(Dispatchers.IO) {
        Users.update(
            where = { Users.id eq id },
            body = {
                it[passwordHash] = newPassword.hashed()
                it[mustChangePassword] = false
            }
        )
    }
}