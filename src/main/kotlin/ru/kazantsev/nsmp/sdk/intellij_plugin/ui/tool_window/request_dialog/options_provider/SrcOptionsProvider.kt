package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider

import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import java.util.concurrent.CompletableFuture

interface SrcOptionsProvider {
    fun loadModulesOptions(): CompletableFuture<List<SrcOption>>

    fun loadScriptsOptions(): CompletableFuture<List<SrcOption>>

    fun loadAdvImportsOptions(): CompletableFuture<List<SrcOption>>
}