package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.model.SrcRequestInputState

@Service(Service.Level.PROJECT)
@State(name = "ProjectInstallationSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ProjectSettingsService : PersistentStateComponent<ProjectSettingsService> {
    var selectedInstallationId: String = ""
    var pullRequestInput: SrcRequestInputState = SrcRequestInputState()
    var syncCheckRequestInput: SrcRequestInputState = SrcRequestInputState()
    var pushRequestInput: SrcRequestInputState = SrcRequestInputState()

    override fun getState(): ProjectSettingsService = this

    override fun loadState(state: ProjectSettingsService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): ProjectSettingsService =
            project.getService(ProjectSettingsService::class.java)
    }
}
