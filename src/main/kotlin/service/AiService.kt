package com.alfoll.service

import com.alfoll.model.AiResponseDTO
import com.alfoll.model.RewriteRequestDTO

interface AiService {
    suspend fun rewrite(text: RewriteRequestDTO): AiResponseDTO
    suspend fun getHistory(): List<AiResponseDTO>
    suspend fun getRecord(id: Int): AiResponseDTO
    suspend fun deleteRecord(id: Int)
}