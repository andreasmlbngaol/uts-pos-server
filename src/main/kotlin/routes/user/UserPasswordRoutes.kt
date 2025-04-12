package com.jawa.routes.user

import com.jawa.dao.UserDao
import com.jawa.response.OtpResponse
import com.jawa.response.stdRespond
import com.jawa.service.PasswordManager
import com.jawa.service.isValidPassword
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.userPasswordRoutes() {
    route("/password") {
        authenticate("auth-admin") {
            patch("/reset") {
                call.extractPatchRequest { id, _ ->
                    val otp = PasswordManager.generateOtp()
                    UserDao.resetPassword(id, otp)
                    call.stdRespond(HttpStatusCode.OK, "Reset password successful", OtpResponse(otp))
                }
            }
        }

        patch("/change") {
            call.extractPatchRequest { id, request ->
                val newPassword = request.newPassword
                val oldPassword = request.oldPassword

                when {
                    oldPassword == null || newPassword == null -> return@extractPatchRequest call.stdRespond(HttpStatusCode.BadRequest, "Old and new password required")
                    !newPassword.isValidPassword() -> return@extractPatchRequest call.stdRespond(HttpStatusCode.BadRequest, "Invalid password format")
                    !UserDao.verifyPassword(id, oldPassword) -> return@extractPatchRequest call.stdRespond(HttpStatusCode.Unauthorized, "Incorrect old password")
                }

                newPassword?.let {
                    UserDao.updatePassword(id, newPassword)
                    call.stdRespond(HttpStatusCode.OK, "Password updated")
                }
            }
        }
    }
}