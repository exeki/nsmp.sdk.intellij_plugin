package ru.kazantsev.nsdplugin.server.dto.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectInfoDto(
    val name: String,
    val basePath: String?,
    val sourceRoots: List<SourceRootDto>,
)
