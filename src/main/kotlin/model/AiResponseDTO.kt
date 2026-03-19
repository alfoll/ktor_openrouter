package com.alfoll.model

import kotlinx.serialization.Serializable

@Serializable
data class AiResponseDTO(
    val id: Int,
    val originalText: String,
    val aiResponse: String,
    val createdAt: String,
)