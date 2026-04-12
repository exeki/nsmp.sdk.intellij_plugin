package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroovyFileResponse(
    val status: String,
    val projectName: String,
    val fileName: String,
    val filePath: String,
)
