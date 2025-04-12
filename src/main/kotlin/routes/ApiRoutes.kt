package com.jawa.routes

import com.jawa.response.stdRespond
import com.jawa.routes.admin.adminRoutes
import com.jawa.routes.auth.authRoutes
import com.jawa.routes.user.userRoutes
import io.ktor.http.*
import io.ktor.server.routing.*

fun Route.apiRoutes() {
    route("/api") {
        get("/") { call.stdRespond(HttpStatusCode.OK, "Server is running successfully!") }

        userRoutes()
        authRoutes()
        adminRoutes()
    }
}