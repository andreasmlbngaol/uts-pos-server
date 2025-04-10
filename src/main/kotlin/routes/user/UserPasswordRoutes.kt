package com.jawa.routes.user

import com.jawa.dao.UserDao
import com.jawa.service.PasswordManager
import com.jawa.service.isValidPassword
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userPasswordRoutes() {
    route("/password") {
        patch("/reset") {
            call.extractPatchRequest { id, request ->
                request.resetPassword?.let { resetPassword ->
                    if (resetPassword) {
                        val otp = PasswordManager.generateOtp()
                        UserDao.resetPassword(id, otp)
                        call.respond(HttpStatusCode.OK, mapOf("otp" to otp))
                    }
                } ?: call.respond(HttpStatusCode.BadRequest, "Invalid Reset Password Request Format")
            }
        }

        patch("/change") {
            call.extractPatchRequest { id, request ->
                val newPassword = request.newPassword
                val oldPassword = request.oldPassword

                when {
                    oldPassword == null || newPassword == null -> return@extractPatchRequest call.respond(HttpStatusCode.BadRequest, "Old and new password required")
                    !newPassword.isValidPassword() -> return@extractPatchRequest call.respond(HttpStatusCode.BadRequest, "Invalid password format")
                    !UserDao.verifyPassword(id, oldPassword) -> return@extractPatchRequest call.respond(HttpStatusCode.Unauthorized, "Incorrect old password")
                }

                newPassword?.let {
                    UserDao.updatePassword(id, newPassword)
                    call.respond(HttpStatusCode.NoContent, "Password updated")
                }
            }
        }
    }
}