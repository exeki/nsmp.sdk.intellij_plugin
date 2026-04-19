package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOption
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto.SrcOptionRoot
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

    fun getOptions(lang: String? = null): SrcOptionRoot {
        return runBlocking {
            val connector = createConnector()
            val scriptsAsync = async(Dispatchers.IO) { connector.getScriptOptions(lang).options }
            val modulesAsync = async(Dispatchers.IO) { connector.getModuleOptions().options }
            val advImportsAsync = async(Dispatchers.IO) { connector.getAdvImportOptions(lang).options }
            SrcOptionRoot(
                scripts = scriptsAsync.await(),
                modules = modulesAsync.await(),
                advImports = advImportsAsync.await(),
            )
        }
    }
}
