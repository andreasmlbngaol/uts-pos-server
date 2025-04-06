package com.jawa

import com.jawa.auth.SESSION_TIMEOUT_IN_SECONDS
import com.jawa.dao.UserSessionDao
import com.jawa.db.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    startSessionCleanupJob()

    configureSerialization()
    configureSession()
    configureUserRoute()
}

@OptIn(DelicateCoroutinesApi::class)
fun startSessionCleanupJob() {
    GlobalScope.launch {
        while (true) {
            delay(SESSION_TIMEOUT_IN_SECONDS.toDuration(DurationUnit.SECONDS))
            UserSessionDao.deleteExpiredSessions()
        }
    }
}