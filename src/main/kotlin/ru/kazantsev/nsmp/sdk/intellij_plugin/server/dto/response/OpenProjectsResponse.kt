package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.ProjectInfoDto

@Serializable
data class OpenProjectsResponse(
    val projects: List<ProjectInfoDto>
)


