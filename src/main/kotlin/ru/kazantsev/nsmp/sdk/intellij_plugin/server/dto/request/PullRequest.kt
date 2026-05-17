package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PullRequest(
    val projectPath: String,
    val type : String,
    val code : String
)
