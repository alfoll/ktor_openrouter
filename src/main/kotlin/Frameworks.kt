package com.alfoll

import com.alfoll.database.configureDatabases
import com.alfoll.database.repository.AiRepository
import com.alfoll.database.repository.AiRepositoryImpl
import com.alfoll.exception.OpenRouterException
import com.alfoll.exception.RecordNotFoundException
import com.alfoll.service.AiService
import com.alfoll.service.AiServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureFrameworks() {

    val database = configureDatabases()

    install(Koin) {
        modules(
            module {
                single<Database> { database }
                single<AiRepository> { AiRepositoryImpl(get()) }
                single<AiService> {
                    AiServiceImpl(
                        aiRepository = get(),
                        aiUrl = environment.config.property("open_router.base_url").getString(),
                        apiKey = environment.config.property("open_router.api_key").getString(),
                        model = environment.config.property("open_router.model").getString(),)
                }
            }
        )
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request 400")
        }
        exception<OpenRouterException> { call, cause ->
            call.respond(HttpStatusCode.BadGateway, cause.message ?: "Open router error 502")
        }
        exception<RecordNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message ?: "Record not found 404")
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid request body 404")
        }
        exception<Throwable> { call, cause ->
            call.respond(status = HttpStatusCode.InternalServerError, cause.message ?: "Internal Server Error")
        }
    }
}
