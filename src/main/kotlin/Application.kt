package com.jawa

import com.jawa.auth.SESSION_TIMEOUT_IN_SECONDS
import com.jawa.dao.UserSessionDao
import com.jawa.db.DatabaseFactory
import com.jawa.routes.apiRoutes
import com.jawa.routes.installPlugins
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    startSessionCleanupJob()

    installPlugins()
    routing { apiRoutes() }
}

fun Application.startSessionCleanupJob() {
    val job = CoroutineScope(Dispatchers.IO).launch {
        while (isActive) {
            delay(SESSION_TIMEOUT_IN_SECONDS.toDuration(DurationUnit.SECONDS))
            UserSessionDao.deleteExpiredSessions()
        }
    }

    monitor.subscribe(ApplicationStopped) {
        job.cancel()
    }
}
