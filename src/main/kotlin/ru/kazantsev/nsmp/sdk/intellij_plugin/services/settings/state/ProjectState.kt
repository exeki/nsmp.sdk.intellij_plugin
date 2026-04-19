package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.state

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.model.SrcRequestSelectState

open class ProjectState {

    var selectedInstallationId: String = ""
    var scriptsDirectoryPath: String = "src/main/scripts"
    var modulesDirectoryPath: String = "src/main/modules"
    var advImportsDirectoryPath: String = "src/main/advimports"
    var savedMultiSelectRequestInput: SrcRequestSelectState = SrcRequestSelectState()

    fun translateValues(otherState: ProjectState? = null): ProjectState {
        val state = otherState ?: ProjectState()
        state.selectedInstallationId = selectedInstallationId
        state.scriptsDirectoryPath = scriptsDirectoryPath
        state.modulesDirectoryPath = modulesDirectoryPath
        state.advImportsDirectoryPath = advImportsDirectoryPath
        state.savedMultiSelectRequestInput = savedMultiSelectRequestInput
        return state
    }
}