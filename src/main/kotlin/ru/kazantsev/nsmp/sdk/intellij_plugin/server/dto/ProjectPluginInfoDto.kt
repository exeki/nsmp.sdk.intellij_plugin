package ru.kazantsev.nsmp.sdk.intellij_plugin.server.dto

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService

@Serializable
data class ProjectPluginInfoDto(
    val installationId: String,
    val installationHost: String,
    val srcFoldersParams : SrcFoldersParamsDto
) {
    companion object {
        fun fromOrNull(settings: ProjectSettingsService): ProjectPluginInfoDto? {
            if (!settings.installationIsSpecified()) return null
            val connectorParams = settings.connectorParams
            return ProjectPluginInfoDto(
                installationId = connectorParams.installationId,
                installationHost = connectorParams.host,
                srcFoldersParams = SrcFoldersParamsDto.from(settings.srcFoldersParams)
            )
        }
    }
}
