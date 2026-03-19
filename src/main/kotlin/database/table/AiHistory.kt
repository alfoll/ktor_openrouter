package com.alfoll.database.table

import org.jetbrains.exposed.dao.id.IntIdTable

object AiHistory: IntIdTable("ai_history") {
    val originalText = text("original_text")
    val aiResponse = text("ai_response")
    val createdAt = text("created_at")
}