package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.InstallationConfigFileService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.NsdPluginSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectInstallationSettingsService
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

        val connectorParams = ConnectorParams(
            connectorInstallation.installationId,
            connectorInstallation.scheme,
            connectorInstallation.host,
            connectorInstallation.accessKey,
            connectorInstallation.ignoreSSL
        )

        return SrcService(
            SrcConnector(connectorParams),
            ObjectMapper(),
            Paths.get(projectBasePath)
        )
    }

    private fun resolveConnectorInstallation(): ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ConnectorInstallation {
        val projectSettings = ProjectInstallationSettingsService.getInstance(project)
        val selectedInstallationId = projectSettings.selectedInstallationId
        if (selectedInstallationId.isBlank()) {
            throw IllegalStateException(MyMessageBundle.message("sync.error.installation.not.selected"))
        }

        val appSettings = NsdPluginSettingsService.getInstance()
        val installationConfigFileService = ApplicationManager.getApplication().getService(InstallationConfigFileService::class.java)
        val installation = installationConfigFileService
            .load(appSettings.connectorConfigPath)
            .firstOrNull { it.installationId == selectedInstallationId }

        return installation ?: throw IllegalStateException(
            MyMessageBundle.message("sync.error.installation.not.found", selectedInstallationId)
        )
    }
}
