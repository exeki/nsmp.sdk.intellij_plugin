package ru.kazantsev.nsmp.sdk.intellij_plugin.services.project

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService

@Service(Service.Level.PROJECT)
class SourceRootMarkerService(private val project: Project) {

    private val projectSettingsService = project.service<ProjectSettingsService>()

    private val srcFoldersParams = projectSettingsService.srcFoldersParams

    val srcRoots = setOf(
        srcFoldersParams.getScriptsRelativePathString(),
        srcFoldersParams.getModulesRelativePathString(),
        srcFoldersParams.getAdvImportsRelativePathString()
    )

    fun markConfiguredRoots() {
        if (project.isDisposed) return

        val modules = ModuleManager.getInstance(project).modules
        modules.forEach { module ->
            var changed = false
            ModuleRootModificationUtil.updateModel(module) { model ->
                model.contentEntries.forEach { entry ->
                    changed = ensureConfigured(entry) || changed
                }
            }

            if (changed) {
                logger.info("Updated source/resource roots for module '${module.name}'")
            }
        }
    }

    private fun ensureConfigured(entry: ContentEntry): Boolean {
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
        rootType: org.jetbrains.jps.model.module.JpsModuleSourceRootType<*>,
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

    private companion object {
        private val logger = thisLogger()
    }
}

