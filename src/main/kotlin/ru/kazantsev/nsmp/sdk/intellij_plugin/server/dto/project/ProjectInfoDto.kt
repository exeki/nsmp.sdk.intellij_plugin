package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectInfoDto(
    val name: String,
    val basePath: String?,
    val sourceRoots: List<SourceRootDto>,
)
