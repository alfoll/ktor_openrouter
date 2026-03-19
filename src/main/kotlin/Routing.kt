package com.alfoll

import com.alfoll.database.table.AiHistory
import com.alfoll.model.AiResponseDTO
import com.alfoll.model.RewriteRequestDTO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Application.configureRouting() {
    routing {
        post("/text") {
            val request = call.receive<RewriteRequestDTO>()
            val textId = transaction {
                AiHistory.insert {
                    it[originalText] = request.text
                    it[aiResponse] = "Ai response"
                    it[createdAt] = LocalDateTime.now().toString()
                } get AiHistory.id
            }
            call.respond(HttpStatusCode.Created, textId.value)
        }

        get("/text") {
            val all = transaction {
                AiHistory.selectAll().map { row ->
                    AiResponseDTO(
                        id = row[AiHistory.id].value,
                        originalText = row[AiHistory.originalText],
                        aiResponse = row[AiHistory.aiResponse],
                        createdAt = row[AiHistory.createdAt]
                    )
                }
            }
            call.respond(all)
        }
    }

}
