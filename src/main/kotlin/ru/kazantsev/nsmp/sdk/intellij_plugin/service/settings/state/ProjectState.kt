package ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.state

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState

open class ProjectState {
    var selectedInstallationId: String = ""
    var scriptsDirectoryPath: String = "src/main/scripts"
    var modulesDirectoryPath: String = "src/main/modules"
    var advImportsDirectoryPath: String = "src/main/advimports"
    var savedMultiSelectRequestInput: SrcRequestSelectState = SrcRequestSelectState()
    var executeContexts: MutableMap<String, String> = mutableMapOf()
    var topPanelCollapsed: Boolean = false
}
