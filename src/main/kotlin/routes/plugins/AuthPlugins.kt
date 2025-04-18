package com.jawa.routes.plugins

import com.jawa.auth.SESSION_TIMEOUT_IN_SECONDS
import com.jawa.auth.UserSession
import com.jawa.dao.UserSessionDao
import com.jawa.enums.Role
import com.jawa.response.stdRespond
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*

fun Application.installAuth() {
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
            challenge { call.stdRespond(HttpStatusCode.Unauthorized, "Unauthorized Admin") }
        }

        session<UserSession>("auth-cashier") {
            validate {
                val session = UserSessionDao.getSession(it.token)
                val valid = session != null && it.role == Role.CASHIER
                if (valid) it else null
            }
            challenge { call.stdRespond(HttpStatusCode.Unauthorized, "Unauthorized Cashier") }
        }
    }
}