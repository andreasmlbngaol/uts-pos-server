package com.jawa.routes.admin

import com.jawa.auth.userSession
import com.jawa.response.stdRespond
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
    authenticate("auth-admin") {
        get("/hello") {
            val userSession = call.userSession!!
            call.stdRespond(HttpStatusCode.OK, "Hello ${userSession.userId} (${userSession.role})!")
        }
    }
}