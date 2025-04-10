package com.jawa.routes

import com.jawa.routes.auth.authRoutes
import com.jawa.routes.user.userRoutes
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.apiRoutes() {
    route("/api") {
        get("/") { call.respond(HttpStatusCode.OK, "Hello From Ktor") }

        userRoutes()
        authRoutes()
    }
}