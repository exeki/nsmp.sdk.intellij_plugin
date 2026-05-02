package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.global_src_request_dialog.options_provider

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.dto.SrcOption
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcSetRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import java.util.concurrent.CompletableFuture

class LocalSrcOptionsProvider(
    project: Project,
) : SrcOptionsProvider {
    private val syncService = project.service<SyncService>()
    private var modulesOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var scriptsOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var advImportsOptionsFuture: CompletableFuture<List<SrcOption>>? = null

    override fun loadModulesOptions(): CompletableFuture<List<SrcOption>> {
        val future = modulesOptionsFuture
        if (future != null) return future
        val req = SrcSetRequest(type = SrcType.MODULE, all = true)
        return CompletableFuture.supplyAsync(
            { syncService.createSrcSyncService().localSrcService.getLocalSrcSet(req).map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    override fun loadScriptsOptions(): CompletableFuture<List<SrcOption>> {
        val future = scriptsOptionsFuture
        if (future != null) return future
        val req = SrcSetRequest(type = SrcType.SCRIPT, all = true)
        return CompletableFuture.supplyAsync(
            { syncService.createSrcSyncService().localSrcService.getLocalSrcSet(req).map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    override fun loadAdvImportsOptions(): CompletableFuture<List<SrcOption>> {
        val future = advImportsOptionsFuture
        if (future != null) return future
        val req = SrcSetRequest(type = SrcType.ADV_IMPORT, all = true)
        return CompletableFuture.supplyAsync(
            { syncService.createSrcSyncService().localSrcService.getLocalSrcSet(req).map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    private fun toSrcOption(dto: SrcFileChecksum): SrcOption {
        return SrcOption(
            code = dto.code,
            title = dto.file.name,
        )
    }
}
