package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.exception.UnselectedProjectInstallation
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.state.ProjectState
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService

@Service(Service.Level.PROJECT)
@State(name = "ProjectInstallationSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ProjectSettingsService(private val project: Project) : PersistentStateComponent<ProjectState> {

    var settings = ProjectState()

    override fun getState(): ProjectState {
        return this.settings
    }

    override fun loadState(state: ProjectState) {
        this.settings = state
    }

    val appSettings: AppSettingsService
        get() = ApplicationManager.getApplication().service<AppSettingsService>()

    val projectPath: String
        get() = project.basePath ?: throw IllegalStateException("Project is not set")

    val connectorParams: ConnectorParams
        get() {
            val selectedInstallationId = settings.selectedInstallationId
            if (selectedInstallationId.isBlank()) {
                throw UnselectedProjectInstallation(MessageBundle.message("sync.error.installation.not.selected"))
            }

            return ConnectorParams.byConfigFileInPath(
                selectedInstallationId,
                appSettings.pathToConfigFile
            )
        }

    val srcFoldersParams: SrcFoldersParams
        get() {
            return SrcFoldersParams(
                projectAbsolutePath = projectPath,
                scriptsRelativePath = settings.scriptsDirectoryPath.trim(),
                modulesRelativePath = settings.modulesDirectoryPath.trim(),
                advImportsRelativePath = settings.advImportsDirectoryPath.trim(),
            )
        }

    val srcService: SrcSyncService
        get() {
            return SrcSyncService(
                connectorParams,
                ObjectMapper(),
                srcFoldersParams
            )
        }

    fun checkInstallationIsSpecified() : Boolean {
        if(state.selectedInstallationId.isNotEmpty()) return true
        //TODO заменить на бандл
        project.service<DialogNotificationService>().showError(
            "Project installation not found",
            "Project installation is not specified. Please specify installation in settings."
        )
        return false
    }
}
