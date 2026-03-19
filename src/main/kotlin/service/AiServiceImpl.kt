package com.alfoll.service

import com.alfoll.database.repository.AiRepository
import com.alfoll.model.AiResponseDTO
import com.alfoll.model.RewriteRequestDTO
import java.time.LocalDateTime

class AiServiceImpl(
    private val aiRepository: AiRepository
): AiService {

    override suspend fun rewrite(text: RewriteRequestDTO): AiResponseDTO {
        require(text.text.isNotBlank()) { "Text must not be blank" }

        val aiResponse = "Ai response" // заглушка на ии ответ
        val createdAt = LocalDateTime.now()

        return aiRepository.create(
            text.text,
            aiResponse,
            createdAt.toString()
        )
    }

    override suspend fun getHistory(): List<AiResponseDTO> {
        return aiRepository.getAll()
    }

    override suspend fun getRecord(id: Int): AiResponseDTO {
        require(id > 0 ) { "Record id cannot be negative" }

        return aiRepository.getById(id)?: error("Record not found") // !! переделать на 404
    }

    override suspend fun deleteRecord(id: Int) {
        require(id > 0) { "Record id cannot be negative" }
        val deleted = aiRepository.deleteById(id)
        if (!deleted)
            error("Record not found") // сделать 404
    }
}