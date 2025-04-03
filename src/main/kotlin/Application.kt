package com.jawa

import com.jawa.db.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)

    configureSerialization()
    configureUserRoute()
}
