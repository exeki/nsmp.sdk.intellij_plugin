package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.basic_api_connector.ConfigService
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.basic_api_connector.dto.InstallationDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.sources_sync.SrcConnector
import ru.kazantsev.nsmp.sdk.sources_sync.SrcService
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import java.nio.file.Paths

@Service(Service.Level.PROJECT)
class SrcSyncProjectService(private val project: Project) {
    fun pull(request: SrcRequest): SrcDtoRoot = createSrcService().pull(request)

    fun syncCheck(request: SrcRequest): SrcInfoRoot = createSrcService().syncCheck(request)

    fun push(request: SrcRequest): SrcInfoRoot = createSrcService().push(request)

    private fun createSrcService(): SrcService {
        val connectorInstallation = resolveConnectorInstallation()
        val projectBasePath = project.basePath ?: throw IllegalStateException(
            MyMessageBundle.message("sync.error.project.path.missing")
        )

        val connectorParams = ConnectorParams.fromDto(connectorInstallation)

        return SrcService(
            SrcConnector(connectorParams),
            ObjectMapper(),
            Paths.get(projectBasePath)
        )
    }

    private fun resolveConnectorInstallation(): InstallationDto {
        val projectSettings = ProjectSettingsService.getInstance(project)
        val selectedInstallationId = projectSettings.selectedInstallationId
        if (selectedInstallationId.isBlank()) {
            throw IllegalStateException(MyMessageBundle.message("sync.error.installation.not.selected"))
        }

        val appSettings = AppSettingsService.getInstance()
        val installation = ConfigService(appSettings.connectorConfigPath).getInstallation(selectedInstallationId)

        return installation ?: throw IllegalStateException(
            MyMessageBundle.message("sync.error.installation.not.found", selectedInstallationId)
        )
    }
}
