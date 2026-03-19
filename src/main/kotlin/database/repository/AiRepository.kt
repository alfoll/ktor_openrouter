package com.alfoll.database.repository

import com.alfoll.model.AiResponseDTO

interface AiRepository {
    suspend fun create(originalText: String, aiResponse: String, createdAt: String): AiResponseDTO
    suspend fun getAll(): List<AiResponseDTO>
    suspend fun getById(id: Int): AiResponseDTO?
    suspend fun deleteById(id: Int): Boolean
}