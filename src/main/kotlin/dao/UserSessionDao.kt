package com.jawa.dao

import com.jawa.auth.UserSession
import com.jawa.entities.UserSessions
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserSessionDao {
    suspend fun deleteExpiredSessions() = newSuspendedTransaction(Dispatchers.IO) {
        val expired = UserSessions.deleteWhere {
            expiredAt lessEq System.currentTimeMillis()
        }
        if (expired > 0) {
            println("Deleted $expired expired session${if (expired > 1) "s" else ""}")
        }
    }

    suspend fun insertNewSession(userSession: UserSession) = newSuspendedTransaction(Dispatchers.IO) {
        UserSessions.insert {
            it[userId] = userSession.userId
            it[token] = userSession.token
            it[expiredAt] = userSession.expiredAt
        }
    }

    suspend fun getSession(token: String) = newSuspendedTransaction(Dispatchers.IO) {
        UserSessions
            .selectAll()
            .where {
                UserSessions.token eq token
                UserSessions.expiredAt greaterEq System.currentTimeMillis()
            }
            .singleOrNull()
    }
}