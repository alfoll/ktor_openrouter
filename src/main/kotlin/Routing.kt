package com.alfoll

import com.alfoll.model.RewriteRequestDTO
import com.alfoll.service.AiService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val aiService by inject<AiService>()

    routing {
        route("/ai") {

            post("/rewrite") {
                val text = call.receive<RewriteRequestDTO>()
                val response = aiService.rewrite(text)
                call.respond(HttpStatusCode.OK, response)
            }

            get("/history") {
                val history = aiService.getHistory()
                call.respond(HttpStatusCode.OK, history)
            }

            get("/history/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid id")

                val record = aiService.getRecord(id)
                call.respond(HttpStatusCode.OK, record)
            }

            delete("/history/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")

                aiService.deleteRecord(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
