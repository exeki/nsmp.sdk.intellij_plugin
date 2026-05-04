package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectPluginInfoDto(
    val installationId: String,
    val installationHost : String
)
