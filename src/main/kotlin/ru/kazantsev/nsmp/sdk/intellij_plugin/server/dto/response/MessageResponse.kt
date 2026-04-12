package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String,
)
