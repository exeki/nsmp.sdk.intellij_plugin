package ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.state

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams

open class ProjectState {
    var selectedInstallationId: String = ""
    var scriptsDirectoryPath: String = SrcFoldersParams.getDefaultRelativeScriptsPathString()
    var modulesDirectoryPath: String = SrcFoldersParams.getDefaultModulesRelativePathString()
    var advImportsDirectoryPath: String = SrcFoldersParams.getDefaultAdvImportsRelativePathString()
    var savedMultiSelectRequestInput: SrcRequestSelectState = SrcRequestSelectState()
    var executeContexts: MutableMap<String, String> = mutableMapOf()
    var topPanelCollapsed: Boolean = false
}