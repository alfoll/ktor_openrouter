package com.alfoll.database

import com.alfoll.database.table.AiHistory
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases(): Database {
    val jdbcUrl = environment.config.property("storage.url").getString()
    val driver = environment.config.property("storage.driver").getString()

    val database = Database.connect(
        url = jdbcUrl,
        driver = driver,
    )

    transaction(database) {
        SchemaUtils.create(AiHistory)
    }

    return database
}
