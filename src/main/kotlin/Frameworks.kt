package com.alfoll

import com.alfoll.database.configureDatabases
import com.alfoll.database.repository.AiRepository
import com.alfoll.database.repository.AiRepositoryImpl
import com.alfoll.service.AiService
import com.alfoll.service.AiServiceImpl
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {

    val database = configureDatabases()

    install(Koin) {
        modules(
            module {
                single<Database> { database }
                single<AiRepository> { AiRepositoryImpl(get()) }
                single<AiService> { AiServiceImpl(get()) }
            }
        )
    }
}
