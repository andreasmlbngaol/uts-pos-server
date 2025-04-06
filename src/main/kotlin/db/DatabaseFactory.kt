package com.jawa.db

import com.jawa.entities.*
import com.jawa.enums.Role
import com.jawa.service.hashed
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.propertyOrNull("postgres.url")?.getString() ?: "jdbc:postgresql://localhost:5432/uts_pos_db"
            driverClassName = config.propertyOrNull("postgres.driver")?.getString() ?: "org.postgresql.Driver"
            username = config.propertyOrNull("postgres.username")?.getString() ?: "andre"
            password = config.propertyOrNull("postgres.password")?.getString() ?: "150503"
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        Database.connect(HikariDataSource(hikariConfig))

        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        transaction {
            SchemaUtils.create(
                Users,
                Products,
                Transactions,
                TransactionDetails,
                UserSessions
            )

            Users.insertIgnore {
                it[username] = "andreasmlbngaol"
                it[name] = "Andreas M Lbn Gaol"
                it[passwordHash] = "password".hashed()
                it[role] = Role.ADMIN
                it[mustChangePassword] = false
            }
        }
    }
}