package com.jawa.entities

import org.jetbrains.exposed.dao.id.LongIdTable

object Products: LongIdTable("products") {
    val name = varchar("name", 255)
    val price = double("price")
    val stock = integer("stock")
}