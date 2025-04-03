package com.jawa.entities

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Transactions: LongIdTable("transactions") {
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val totalAmount = double("total_amount")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}