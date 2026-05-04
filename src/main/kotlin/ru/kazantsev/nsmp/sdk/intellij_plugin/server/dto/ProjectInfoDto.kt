package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

@Serializable
data class ProjectInfoDto(
    val name: String,
    val basePath: String? = null,
    val isDisposed: Boolean,
    val isInitialized: Boolean,
    val nsmpSdk: ProjectPluginInfoDto?,
) {
    companion object {
        fun from(project: Project): ProjectInfoDto =
            from(project, project.service<ProjectSettingsService>())

        fun from(project: Project, settings: ProjectSettingsService): ProjectInfoDto =
            ProjectInfoDto(
                name = project.name,
                basePath = project.basePath,
                isDisposed = project.isDisposed,
                isInitialized = project.isInitialized,
                nsmpSdk = ProjectPluginInfoDto.fromOrNull(settings),
            )
    }
}
