package com.jawa.service

import com.jawa.dao.UserDao

object UserRequestManager {
    suspend fun isUsernameValid(username: String): Boolean {
        val isFormatValid = username.matches("^(?!\\.)([a-z0-9_]+(\\.[a-z0-9_]+)*){6,}(?!\\.)$".toRegex())
        val isAvailable = UserDao.getUserByUsername(username) == null
        return isFormatValid && isAvailable
    }

    fun isNameValid(name: String): Boolean =
        name.isBlank()

    fun isPasswordValid(password: String): Boolean =
        password.isBlank()
}

suspend fun String.validUsername() = UserRequestManager.isUsernameValid(this)
fun String.validName() = UserRequestManager.isNameValid(this)
fun String.validPassword() = UserRequestManager.isPasswordValid(this)