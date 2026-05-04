package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.response

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto.ProjectInfoDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

@Serializable
data class ProjectByPathResponse(
    val project: ProjectInfoDto
) {
    companion object {
        fun from(project: Project): ProjectByPathResponse {
            val settings = project.service<ProjectSettingsService>()
            return ProjectByPathResponse(
                project = ProjectInfoDto.from(project, settings),
            )
        }
    }
}
