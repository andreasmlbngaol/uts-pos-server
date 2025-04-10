package com.jawa.routes.user

import com.jawa.dao.UserDao
import com.jawa.dto.CreateUserRequest
import com.jawa.service.PasswordManager
import com.jawa.service.isUsernameAvailable
import com.jawa.service.isValidName
import com.jawa.service.isValidUsername
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    route("/users") {
        get { call.respond(HttpStatusCode.OK, UserDao.getAllUsers()) }

        post {
            val request = runCatching { call.receive<CreateUserRequest>() }
                .getOrElse {
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid Request")
                }

            when {
                !request.username.isValidUsername() -> return@post call.respond(HttpStatusCode.BadRequest, "Invalid Username")
                !request.username.isUsernameAvailable() -> return@post call.respond(HttpStatusCode.Conflict, "Username is already taken")
                !request.name.isValidName() -> return@post call.respond(HttpStatusCode.BadRequest, "Invalid Name")
            }

            val otp = PasswordManager.generateOtp()
            UserDao.insertUser(
                request = request.copy(username = request.username.lowercase()),
                otp = otp
            )
            call.respond(HttpStatusCode.Created, mapOf("otp" to otp))
        }

        userDetailRoutes()
    }
}