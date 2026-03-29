package ru.kazantsev.nsdplugin.server.dto.project

import kotlinx.serialization.Serializable

@Serializable
data class SourceRootDto (
    val path : String,
    val name : String,
)