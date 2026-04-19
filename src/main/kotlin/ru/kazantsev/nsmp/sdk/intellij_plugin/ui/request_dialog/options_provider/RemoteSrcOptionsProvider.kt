package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.options_provider

import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.SrcOptionsService
import java.util.concurrent.CompletableFuture
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption

class RemoteSrcOptionsProvider(
    private val srcOptionsService: SrcOptionsService,
) : SrcOptionsProvider {
    private var modulesOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var scriptsOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var advImportsOptionsFuture: CompletableFuture<List<SrcOption>>? = null

    override fun loadModulesOptions(): CompletableFuture<List<SrcOption>> {
        val future = modulesOptionsFuture
        if (future != null) return future
        return srcOptionsService.loadModulesOptions().also { modulesOptionsFuture = it }
    }

    override fun loadScriptsOptions(): CompletableFuture<List<SrcOption>> {
        val future = scriptsOptionsFuture
        if (future != null) return future
        return srcOptionsService.loadScriptsOptions().also { scriptsOptionsFuture = it }
    }

    override fun loadAdvImportsOptions(): CompletableFuture<List<SrcOption>> {
        val future = advImportsOptionsFuture
        if (future != null) return future
        return srcOptionsService.loadAdvImportsOptions().also { advImportsOptionsFuture = it }
    }
}
