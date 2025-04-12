package com.jawa.routes.auth

import com.jawa.auth.LoginRequest
import com.jawa.auth.UserSession
import com.jawa.dao.UserDao
import com.jawa.dao.UserDao.toUser
import com.jawa.dao.UserSessionDao
import com.jawa.entities.Users
import com.jawa.response.stdRespond
import com.jawa.service.PasswordManager
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.authRoutes() {
    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val user = UserDao.getUserByUsername(loginRequest.username)
        if (user == null || !PasswordManager.verifyPassword(loginRequest.password, user[Users.passwordHash]))
            return@post call.stdRespond(HttpStatusCode.Unauthorized, "Username or password is incorrect")

        val session = UserSession(
            userId = user[Users.id].value,
            token = UUID.randomUUID().toString(),
            role = user[Users.role]
        )
        call.sessions.set(session)
        UserSessionDao.insertNewSession(session)

        call.stdRespond(HttpStatusCode.OK, "Login Success", user.toUser())
    }

    post("/logout") {
        val session = call.sessions.get<UserSession>()
            ?: return@post call.stdRespond(HttpStatusCode.Unauthorized, "You are not logged in")

        UserSessionDao.deleteSession(session.token)
        call.sessions.clear<UserSession>()

        call.stdRespond(HttpStatusCode.OK, "Logout Success")

    }
}