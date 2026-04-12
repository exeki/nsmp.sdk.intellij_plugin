package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.project

import kotlinx.serialization.Serializable

@Serializable
data class SourceRootDto (
    val path : String,
    val name : String,
)