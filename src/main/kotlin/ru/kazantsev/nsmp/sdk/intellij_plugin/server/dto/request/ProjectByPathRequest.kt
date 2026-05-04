package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ProjectByPathRequest(
    val path: String,
)
