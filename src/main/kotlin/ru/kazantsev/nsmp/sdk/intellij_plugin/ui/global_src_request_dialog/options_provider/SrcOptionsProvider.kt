package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.dto.SrcOption
import java.util.concurrent.CompletableFuture

interface SrcOptionsProvider {
    fun loadModulesOptions(): CompletableFuture<List<SrcOption>>

    fun loadScriptsOptions(): CompletableFuture<List<SrcOption>>

    fun loadAdvImportsOptions(): CompletableFuture<List<SrcOption>>
}