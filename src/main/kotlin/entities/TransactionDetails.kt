package com.jawa.entities

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TransactionDetails: LongIdTable("transaction_details") {
    val transactionId = reference("transaction_id", Transactions.id, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val totalPrice = double("total_price")
}