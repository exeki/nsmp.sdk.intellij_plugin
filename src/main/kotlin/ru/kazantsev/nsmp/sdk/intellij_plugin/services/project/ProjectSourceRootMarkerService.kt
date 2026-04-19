package ru.kazantsev.nsmp.sdk.intellij_plugin.services.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType

@Service(Service.Level.PROJECT)
class ProjectSourceRootMarkerService(
    private val project: Project,
) : Disposable {
    init {
        project.messageBus.connect(this).subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    if (events.any(::isRelevantEvent)) {
                        markConfiguredRoots()
                    }
                }
            }
        )
        project.messageBus.connect(this).subscribe(
            ProjectDataImportListener.TOPIC,
            object : ProjectDataImportListener {
                override fun onImportFinished(projectPath: String?) {
                    logger.info("Build system sync finished for '${projectPath.orEmpty()}', refreshing source/resource roots")
                    markConfiguredRoots()
                }
            }
        )
    }

    override fun dispose() = Unit

    fun markConfiguredRoots() {
        if (project.isDisposed) {
            return
        }

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

        changed = ensureSourceRoot(entry, contentRoot, "src/main/scripts", JavaSourceRootType.SOURCE) || changed
        changed = ensureSourceRoot(entry, contentRoot, "src/main/modules", JavaSourceRootType.SOURCE) || changed
        changed = ensureSourceRoot(entry, contentRoot, "src/main/advimports", JavaResourceRootType.RESOURCE) || changed

        return changed
    }

    private fun ensureSourceRoot(
        entry: ContentEntry,
        contentRoot: VirtualFile,
        relativePath: String,
        rootType: org.jetbrains.jps.model.module.JpsModuleSourceRootType<*>,
    ): Boolean {
        val target = contentRoot.findFileByRelativePath(relativePath) ?: return false
        if (!target.isDirectory) {
            return false
        }

        val existing = entry.sourceFolders.firstOrNull { sourceFolder -> sourceFolder.file == target }
        if (existing == null) {
            entry.addSourceFolder(target, rootType)
            return true
        }

        if (existing.rootType == rootType) {
            return false
        }

        entry.removeSourceFolder(existing)
        entry.addSourceFolder(target, rootType)
        return true
    }

    private fun isRelevantEvent(event: VFileEvent): Boolean {
        val path = event.path.replace('\\', '/')
        return path.contains("/src/main/scripts") ||
            path.contains("/src/main/modules") ||
            path.contains("/src/main/advimports")
    }

    private companion object {
        private val logger = thisLogger()
    }
}
