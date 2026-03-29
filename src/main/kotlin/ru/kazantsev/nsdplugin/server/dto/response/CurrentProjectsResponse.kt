package ru.kazantsev.nsdplugin.server.dto.response

import kotlinx.serialization.Serializable
import ru.kazantsev.nsdplugin.server.dto.project.ProjectInfoDto

@Serializable
data class CurrentProjectsResponse(
    val projects: List<ProjectInfoDto>,
)

