package com.jawa.service

import com.jawa.dao.UserDao

object UserRequestManager {
    suspend fun isUsernameAvailable(username: String): Boolean {
        return UserDao.getUserByUsername(username.lowercase()) == null
    }

    fun isUsernameValid(username: String): Boolean {
        return username.lowercase().matches("^(?!\\.)([a-z0-9_]+(\\.[a-z0-9_]+)*){6,}(?!\\.)$".toRegex())
    }

    fun isNameValid(name: String): Boolean =
        name.isNotBlank()

    fun isPasswordValid(password: String): Boolean =
        password.isNotBlank()
}

suspend fun String.isUsernameAvailable() = UserRequestManager.isUsernameAvailable(this)
fun String.isValidUsername() = UserRequestManager.isUsernameValid(this)
fun String.isValidName() = UserRequestManager.isNameValid(this)
fun String.isValidPassword() = UserRequestManager.isPasswordValid(this)