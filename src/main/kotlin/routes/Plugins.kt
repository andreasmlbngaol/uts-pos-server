package com.jawa.routes

import com.jawa.routes.plugins.installAuth
import com.jawa.routes.plugins.installContentNegotiation
import io.ktor.server.application.*

fun Application.installPlugins() {
    installContentNegotiation()
    installAuth()
}