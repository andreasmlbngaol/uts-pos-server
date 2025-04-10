package com.jawa.routes.auth

import com.jawa.auth.LoginRequest
import com.jawa.auth.UserSession
import com.jawa.dao.UserDao
import com.jawa.dao.UserSessionDao
import com.jawa.entities.Users
import com.jawa.service.PasswordManager
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.authRoutes() {
    route("/api") {
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            val user = UserDao.getUserByUsername(loginRequest.username)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User Not Found")
                return@post
            }

            if (!PasswordManager.verifyPassword(loginRequest.password, user[Users.passwordHash])) {
                call.respond(HttpStatusCode.Unauthorized, "Passwords do not match")
                return@post
            }

            val session = UserSession(
                userId = user[Users.id].value,
                token =  UUID.randomUUID().toString(),
                role = user[Users.role]
            )
            call.sessions.set(session)
            UserSessionDao.insertNewSession(session)

            call.respond(HttpStatusCode.OK, "Login Success")
        }

        authenticate("auth-admin") {
            get("/hello") {
                val userSession = call.principal<UserSession>()!!
                call.respond(HttpStatusCode.OK, "Hello ${userSession.userId} (${userSession.role})!")
            }
        }
    }
}