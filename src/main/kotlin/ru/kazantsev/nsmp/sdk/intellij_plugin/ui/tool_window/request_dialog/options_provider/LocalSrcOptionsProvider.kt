package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider

import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.CompletableFuture
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcFileDto
import ru.kazantsev.nsmp.sdk.sources_sync.service.SrcFolder

class LocalSrcOptionsProvider(
    private val projectSettingsService: ProjectSettingsService,
) : SrcOptionsProvider {
    private var modulesOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var scriptsOptionsFuture: CompletableFuture<List<SrcOption>>? = null
    private var advImportsOptionsFuture: CompletableFuture<List<SrcOption>>? = null

    override fun loadModulesOptions(): CompletableFuture<List<SrcOption>> {
        val future = modulesOptionsFuture
        if (future != null) return future
        return loadOptions(projectSettingsService.srcService.modulesSrcFolder).also { modulesOptionsFuture = it }
    }

    override fun loadScriptsOptions(): CompletableFuture<List<SrcOption>> {
        val future = scriptsOptionsFuture
        if (future != null) return future
        return loadOptions(projectSettingsService.srcService.scriptsSrcFolder).also { scriptsOptionsFuture = it }
    }

    override fun loadAdvImportsOptions(): CompletableFuture<List<SrcOption>> {
        val future = advImportsOptionsFuture
        if (future != null) return future
        return loadOptions(projectSettingsService.srcService.advImportsSrcFolder).also { advImportsOptionsFuture = it }
    }

    private fun loadOptions(folder: SrcFolder): CompletableFuture<List<SrcOption>> {
        return CompletableFuture.supplyAsync(
            { folder.getAllSourceFiles().map { toSrcOption(it) } },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    private fun toSrcOption(dto: SrcFileDto): SrcOption {
        return SrcOption(
            code = dto.code,
            title = dto.file.name,
        )
    }
}
