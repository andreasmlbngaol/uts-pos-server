package com.jawa.dao

import com.jawa.dto.CreateUserRequest
import com.jawa.dto.User
import com.jawa.entities.Users
import com.jawa.service.PasswordManager
import com.jawa.service.hashed
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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

    suspend fun verifyPassword(id: Long, inputPassword: String): Boolean {
        val user = getUserById(id) ?: return false
        return PasswordManager.verifyPassword(inputPassword, user[Users.passwordHash])
    }

    fun ResultRow.toUser(): User {
        return User(
            id = this[Users.id].value,
            username = this[Users.username],
            name = this[Users.name],
            role = this[Users.role],
            mustChangePassword = this[Users.mustChangePassword]
        )
    }
}