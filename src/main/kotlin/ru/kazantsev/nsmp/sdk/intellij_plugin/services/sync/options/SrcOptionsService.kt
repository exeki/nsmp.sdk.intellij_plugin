package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
class SrcOptionsService(private val project: Project) {
    val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    fun createConnector(): SrcOptionsConnector {
        return SrcOptionsConnector(projectSettingsService.connectorParams)
    }

    fun getScriptOptions(lang: String? = null): List<SrcOption> {
        return createConnector().getScriptOptions(lang).options
    }

    fun getAdvImportOptions(lang: String? = null): List<SrcOption> {
        return createConnector().getAdvImportOptions(lang).options
    }

    fun getModuleOptions(): List<SrcOption> {
        return createConnector().getModuleOptions().options
    }

    fun loadScriptsOptions(lang: String? = null): CompletableFuture<List<SrcOption>> {
        return CompletableFuture.supplyAsync(
            { getScriptOptions(lang) },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    fun loadModulesOptions(): CompletableFuture<List<SrcOption>> {
        return CompletableFuture.supplyAsync(
            { getModuleOptions() },
            AppExecutorUtil.getAppExecutorService()
        )
    }

    fun loadAdvImportsOptions(lang: String? = null): CompletableFuture<List<SrcOption>> {
        return CompletableFuture.supplyAsync(
            { getAdvImportOptions(lang) },
            AppExecutorUtil.getAppExecutorService()
        )
    }
}
