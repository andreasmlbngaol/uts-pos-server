package com.jawa

import com.jawa.auth.LoginRequest
import com.jawa.auth.UserSession
import com.jawa.dao.UserDao
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

fun Application.configureSession() {
    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.extensions["SameSite"] = "lax"
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.maxAgeInSeconds = 3600
        }
    }

    install(Authentication) {
        session<UserSession>("auth-admin") {
            validate { session ->
                println("Validating session: $session") // Debug log untuk validasi
                if(session.role == Role.ADMIN) session else {
                    null
                }
            }
            challenge {
                println(
                    call.sessions.toString()
                )
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
        }
        session<UserSession>("auth-cashier") {
            validate { session ->
                if(session.role == Role.CASHIER) session else null
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
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

                val hashedPassword = user[Users.passwordHash]
                if (!PasswordManager.verifyPassword(loginRequest.password, hashedPassword)) {
                    call.respond(HttpStatusCode.Unauthorized, "Passwords do not match")
                    return@post
                }

                val session = UserSession(user[Users.id].value, user[Users.role])
                call.sessions.set(session)
                call.respond(HttpStatusCode.OK, "Login Success")
            }

            authenticate("auth-admin") {
                get("/hello") {
                    val userSession = call.principal<UserSession>()
                    if(userSession == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Unauthenticated user")
                        return@get
                    }

                    call.respond(HttpStatusCode.OK, "Hello ${userSession.userId} (${userSession.role})!")
                }
            }
        }

    }
}