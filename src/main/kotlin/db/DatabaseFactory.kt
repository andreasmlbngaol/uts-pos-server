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
        val hikariConfig = configureDatabase(config)
        Database.connect(HikariDataSource(hikariConfig))
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        transaction {
            createTables()
            insertAdmin(config)
            insertCashier()
        }
    }

    private fun configureDatabase(config: ApplicationConfig): HikariConfig {
        return HikariConfig().apply {
            jdbcUrl = config.propertyOrNull("postgres.url")?.getString()
            driverClassName = config.propertyOrNull("postgres.driver")?.getString()
            username = config.propertyOrNull("postgres.username")?.getString()
            password = config.propertyOrNull("postgres.password")?.getString()
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
    }

    private fun createTables() {
        SchemaUtils.create(
            Users,
            Products,
            Transactions,
            TransactionDetails,
            UserSessions
        )
    }

    private fun insertAdmin(config: ApplicationConfig) {
        Users.insertIgnore {
            it[username] = config.propertyOrNull("admin.username")?.getString() ?: "admin"
            it[name] = config.propertyOrNull("admin.name")?.getString() ?: "Admin"
            it[passwordHash] = (config.propertyOrNull("admin.password")?.getString() ?: "password").hashed()
            it[role] = Role.ADMIN
            it[mustChangePassword] = false
        }
    }

    // Test
    private fun insertCashier() {
        Users.insertIgnore {
            it[username] = "cashier"
            it[name] = "Cashier"
            it[passwordHash] = "password".hashed()
            it[role] = Role.CASHIER
            it[mustChangePassword] = true
        }
    }
}