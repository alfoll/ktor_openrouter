package com.alfoll.database.repository

import com.alfoll.database.table.AiHistory
import com.alfoll.model.AiResponseDTO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AiRepositoryImpl(
    private val db: Database,
): AiRepository {

    override suspend fun create(originalText: String, aiResponse: String, createdAt: String): AiResponseDTO =
        newSuspendedTransaction(db = db) {
            val id = AiHistory.insertAndGetId {
                it[AiHistory.originalText] = originalText
                it[AiHistory.aiResponse] = aiResponse
                it[AiHistory.createdAt] = createdAt
            }.value

            AiResponseDTO(id, originalText, aiResponse, createdAt)
        }

    override suspend fun getAll(): List<AiResponseDTO> =
        newSuspendedTransaction(db = db) {
            AiHistory.selectAll().map { row ->
                AiResponseDTO(
                    id = row[AiHistory.id].value,
                    originalText = row[AiHistory.originalText],
                    aiResponse = row[AiHistory.aiResponse],
                    createdAt = row[AiHistory.createdAt]
                )
            }
        }

    // может возвращать null - обработка в сервисе
    override suspend fun getById(id: Int): AiResponseDTO? =
        newSuspendedTransaction(db = db) {
            AiHistory.selectAll()
                .where { AiHistory.id eq id }
                .singleOrNull()
                ?.let { row ->
                    AiResponseDTO(id = row[AiHistory.id].value,
                    originalText = row[AiHistory.originalText],
                    aiResponse = row[AiHistory.aiResponse],
                    createdAt = row[AiHistory.createdAt]) }

        }

    override suspend fun deleteById(id: Int): Boolean =
        newSuspendedTransaction(db = db) {
            AiHistory.deleteWhere { AiHistory.id eq id } > 0 // возвращает колво удаленных строк
        }
}