package com.jawa.routes.user

import com.jawa.dao.UserDao
import com.jawa.dto.CreateUserRequest
import com.jawa.response.OtpResponse
import com.jawa.response.stdRespond
import com.jawa.service.PasswordManager
import com.jawa.service.isUsernameAvailable
import com.jawa.service.isValidName
import com.jawa.service.isValidUsername
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/users") {
        get {
            call.stdRespond(HttpStatusCode.OK, "Successfully retrieved all users", UserDao.getAllUsers())
        }

        post {
            val request = runCatching { call.receive<CreateUserRequest>() }
                .getOrElse {
                    return@post call.stdRespond(HttpStatusCode.BadRequest, "Invalid payload")
                }

            when {
                !request.username.isValidUsername() -> return@post call.stdRespond(HttpStatusCode.BadRequest, "Invalid username")
                !request.username.isUsernameAvailable() -> return@post call.stdRespond(HttpStatusCode.Conflict, "Username is already taken")
                !request.name.isValidName() -> return@post call.stdRespond(HttpStatusCode.BadRequest, "Invalid name")
            }

            val otp = PasswordManager.generateOtp()
            UserDao.insertUser(
                request = request.copy(username = request.username.lowercase()),
                otp = otp
            )
            call.stdRespond(HttpStatusCode.Created, "User successfully created. Sending OTP...", OtpResponse(otp))
        }

        userDetailRoutes()
    }
}