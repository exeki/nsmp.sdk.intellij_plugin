package ru.kazantsev.nsdplugin.server.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String,
)
