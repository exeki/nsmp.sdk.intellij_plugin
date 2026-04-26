package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider

import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.CompletableFuture
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import ru.kazantsev.nsmp.sdk.sources_sync.SrcSyncService
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.request.SrcSetRequest

class LocalSrcOptionsProvider(
    private val projectSettingsService: ProjectSettingsService,
) : SrcOptionsProvider {
    private val srcSyncService: SrcSyncService
        get() = projectSettingsService.srcService
    private var modulesOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var scriptsOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var advImportsOptionsFuture: CompletableFuture<List<SrcOption>>? = null

    override fun loadModulesOptions(): CompletableFuture<List<SrcOption>> {
        val future = modulesOptionsFuture
        if (future != null) return future
        val req = SrcSetRequest(type = SrcType.MODULE, all = true)
        return CompletableFuture.supplyAsync(
            { srcSyncService.localSrcService.getLocalSrcSet(req).map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    override fun loadScriptsOptions(): CompletableFuture<List<SrcOption>> {
        val future = scriptsOptionsFuture
        if (future != null) return future
        val req = SrcSetRequest(type = SrcType.SCRIPT, all = true)
        return CompletableFuture.supplyAsync(
            { srcSyncService.localSrcService.getLocalSrcSet(req).map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    override fun loadAdvImportsOptions(): CompletableFuture<List<SrcOption>> {
        val future = advImportsOptionsFuture
        if (future != null) return future
        val req = SrcSetRequest(type = SrcType.ADV_IMPORT, all = true)
        return CompletableFuture.supplyAsync(
            { srcSyncService.localSrcService.getLocalSrcSet(req).map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    private fun toSrcOption(dto: LocalFileInfo): SrcOption {
        return SrcOption(
            code = dto.code,
            title = dto.file.name,
        )
    }
}
