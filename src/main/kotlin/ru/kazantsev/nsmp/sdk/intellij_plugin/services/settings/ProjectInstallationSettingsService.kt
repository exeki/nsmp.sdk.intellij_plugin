package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(name = "ProjectInstallationSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ProjectInstallationSettingsService : PersistentStateComponent<ProjectInstallationSettingsService> {
    var selectedInstallationId: String = ""
    var pullRequestInput: SrcRequestInputState = SrcRequestInputState()
    var syncCheckRequestInput: SrcRequestInputState = SrcRequestInputState()
    var pushRequestInput: SrcRequestInputState = SrcRequestInputState()

    override fun getState(): ProjectInstallationSettingsService = this

    override fun loadState(state: ProjectInstallationSettingsService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): ProjectInstallationSettingsService =
            project.getService(ProjectInstallationSettingsService::class.java)
    }
}

class SrcRequestInputState {
    var modulesCsv: String = ""
    var allModules: Boolean = false
    var scriptsCsv: String = ""
    var allScripts: Boolean = false
    var advImportsCsv: String = ""
    var allAdvImports: Boolean = false
}
