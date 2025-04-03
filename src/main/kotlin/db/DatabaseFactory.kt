package com.jawa.db

import com.jawa.entities.Products
import com.jawa.entities.TransactionDetails
import com.jawa.entities.Transactions
import com.jawa.entities.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.propertyOrNull("postgres.url")?.getString()
            driverClassName = config.propertyOrNull("postgres.driver")?.getString()
            username = config.propertyOrNull("postgres.username")?.getString()
            password = config.propertyOrNull("postgres.password")?.getString()
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
                TransactionDetails
            )
        }

    }
}