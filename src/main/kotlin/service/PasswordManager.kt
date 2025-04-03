package com.jawa.service

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.SecureRandom

object PasswordManager {
    private const val SALT_LENGTH = 12
    private const val OTP_LENGTH = 8

    fun hashPassword(realPassword: String): String =
        BCrypt.withDefaults().hashToString(SALT_LENGTH, realPassword.toCharArray())

    fun verifyPassword(inputPassword: String, hashedPassword: String): Boolean =
        BCrypt.verifyer().verify(hashedPassword.toCharArray(), inputPassword.toCharArray()).verified

    fun generateOtp(): String {
        val characters = "abcdefghijklmnopqrstuvwxyz"
        val random = SecureRandom()
        return (1..OTP_LENGTH)
            .map { characters[random.nextInt(characters.length)] }
            .joinToString("")
    }
}

fun String.hashed() = PasswordManager.hashPassword(this)