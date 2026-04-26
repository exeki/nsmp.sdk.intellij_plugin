package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcRequest

/**
 * Модель последней сохраненной формы
 */
class SrcRequestSelectState {
    var allAdvImports: Boolean = false
    var advImports: MutableList<SelectedSrcOption> = mutableListOf()
    var advImportsExcluded: MutableList<SelectedSrcOption> = mutableListOf()

    var scripts: MutableList<SelectedSrcOption> = mutableListOf()
    var allScripts: Boolean = false
    var scriptsExcluded: MutableList<SelectedSrcOption> = mutableListOf()

    var allModules: Boolean = false
    var modules: MutableList<SelectedSrcOption> = mutableListOf()
    var modulesExcluded: MutableList<SelectedSrcOption> = mutableListOf()

    var force: Boolean = false

    fun getRequest() : SrcRequest {
        return SrcRequest(
            modules = modules.map { it.code }.toSet(),
            allModules = allModules,
            modulesExcluded = modulesExcluded.map { it.code }.toSet(),

            scripts = scripts.map { it.code }.toSet(),
            allScripts = allScripts,
            scriptsExcluded = scriptsExcluded.map { it.code }.toSet(),

            advImports = advImports.map { it.code }.toSet(),
            allAdvImports = allAdvImports,
            advImportsExcluded = advImportsExcluded.map { it.code }.toSet(),
        )
    }
}
