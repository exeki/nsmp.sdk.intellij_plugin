package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.project.ProjectInfoDto

@Serializable
data class CurrentProjectsResponse(
    val projects: List<ProjectInfoDto>,
)
