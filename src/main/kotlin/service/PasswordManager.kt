package com.jawa.service

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordManager {
    private const val SALT_LENGTH = 12
    fun hashPassword(realPassword: String) =
        BCrypt.withDefaults().hashToString(SALT_LENGTH, realPassword.toCharArray())

    fun verifyPassword(inputPassword: String, hashedPassword: String): Boolean =
        BCrypt.verifyer().verify(hashedPassword.toCharArray(), inputPassword.toCharArray()).verified
}

fun String.hashed() = PasswordManager.hashPassword(this)