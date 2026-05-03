package ru.kazantsev.nsmp.sdk.intellij_plugin.unused_concepts.source_roots

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.module.JpsModuleSourceRootType
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService
import ru.kazantsev.nsmp.sdk.sources_sync.SrcFoldersParams
import kotlin.collections.forEach

@Suppress("unused")
@Service(Service.Level.PROJECT)
class SourceRootMarkerService(private val project: Project) {

    private val projectSettingsService = project.getService(ProjectSettingsService::class.java)

    private val pluginActivationService = project.getService(PluginActivationService::class.java)

    fun logModule(module: Module) {
        System.err.println("name: " + module.name)
        //System.err.println("moduleTypeName: " +module.moduleTypeName)
        //System.err.println("moduleFilePath: " +module.moduleFilePath)
        //System.err.println("moduleFile?.path: " +module.moduleFile?.path)
    }

    fun markConfiguredRoots() {
        val srcFoldersParams = projectSettingsService.srcFoldersParams
        if (!pluginActivationService.pluginActive) return
        if (project.isDisposed) return

        val moduleManager = ModuleManager.getInstance(project)
        //moduleManager.findModuleByName()
        val modules = moduleManager.modules
        modules.forEach { module ->
            logModule(module)
            var changed = false
            ModuleRootModificationUtil.updateModel(module) { model ->
                model.contentEntries.forEach { entry ->
                    changed = ensureConfigured(entry, srcFoldersParams) || changed
                }
            }

        }
    }

    private fun ensureConfigured(
        entry: ContentEntry,
        srcFoldersParams: SrcFoldersParams
    ): Boolean {
        val contentRoot = entry.file ?: return false
        var changed = false
        changed = ensureSourceRoot(
            entry,
            contentRoot,
            srcFoldersParams.getScriptsRelativePathString(),
            JavaSourceRootType.SOURCE
        ) || changed
        changed = ensureSourceRoot(
            entry,
            contentRoot,
            srcFoldersParams.getModulesRelativePathString(),
            JavaSourceRootType.SOURCE
        ) || changed
        changed = ensureSourceRoot(
            entry,
            contentRoot,
            srcFoldersParams.getAdvImportsRelativePathString(),
            JavaResourceRootType.RESOURCE
        ) || changed

        return changed
    }

    private fun ensureSourceRoot(
        entry: ContentEntry,
        contentRoot: VirtualFile,
        relativePath: String,
        rootType: JpsModuleSourceRootType<*>,
    ): Boolean {
        val target = contentRoot.findFileByRelativePath(relativePath) ?: return false
        if (!target.isDirectory) return false

        val existing = entry.sourceFolders.firstOrNull { sourceFolder -> sourceFolder.file == target }
        if (existing == null) {
            entry.addSourceFolder(target, rootType)
            return true
        }

        if (existing.rootType == rootType) return false

        entry.removeSourceFolder(existing)
        entry.addSourceFolder(target, rootType)
        return true
    }

}