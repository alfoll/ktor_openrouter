package com.alfoll.model

import kotlinx.serialization.Serializable

@Serializable
data class RewriteRequestDTO(
    val text: String,
)
