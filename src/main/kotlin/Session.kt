package com.jawa

import com.jawa.auth.LoginRequest
import com.jawa.auth.SESSION_TIMEOUT_IN_SECONDS
import com.jawa.auth.UserSession
import com.jawa.dao.UserDao
import com.jawa.dao.UserSessionDao
import com.jawa.entities.Users
import com.jawa.enums.Role
import com.jawa.service.PasswordManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.UUID

fun Application.configureSession() {
    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.extensions["SameSite"] = "lax"
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.maxAgeInSeconds = SESSION_TIMEOUT_IN_SECONDS
        }
    }

    install(Authentication) {
        session<UserSession>("auth-admin") {
            validate {
                val session = UserSessionDao.getSession(it.token)
                val valid = session != null && it.role == Role.ADMIN
                if (valid) it else null
            }
            challenge {call.respond(HttpStatusCode.Unauthorized, "Unauthorized") }
        }

        session<UserSession>("auth-cashier") {
            validate { UserSessionDao.getSession(it.token) ?: if(it.role == Role.CASHIER) it else null }
            challenge { call.respond(HttpStatusCode.Unauthorized, "Unauthorized") }
        }
    }

    routing {
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
}