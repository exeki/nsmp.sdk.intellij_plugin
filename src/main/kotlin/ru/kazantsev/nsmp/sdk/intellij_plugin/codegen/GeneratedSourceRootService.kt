package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ServiceDirSetupService
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import java.nio.file.Files
import java.nio.file.Path

/**
 * Подключает `.nsmp_sdk/generated/metaclasses` как generated source root модуля проекта.
 */
@Service(Service.Level.PROJECT)
class GeneratedSourceRootService(private val project: Project) {

    fun getOutputDirectoryPath(): Path {
        val basePath = project.basePath ?: throw IllegalStateException("Project base path is not set")
        return Path.of(basePath, MetaClassGenerationSettings.GENERATED_SOURCES_RELATIVE_PATH)
    }

    fun ensureRegistered() {
        project.getService(ServiceDirSetupService::class.java).ensureServiceDirLayout()

        val outputDirectory = getOutputDirectoryPath()
        Files.createDirectories(outputDirectory)

        ApplicationManager.getApplication().invokeAndWait {
            WriteAction.run<RuntimeException> {
                val virtualFile = LocalFileSystem.getInstance()
                    .refreshAndFindFileByPath(outputDirectory.toString().replace('\\', '/'))
                    ?: return@run

                ensureNotExcluded(virtualFile)

                ModuleManager.getInstance(project).modules.forEach { module ->
                    val model = ModuleRootManager.getInstance(module).modifiableModel
                    try {
                        model.contentEntries.forEach { contentEntry ->
                            val contentRoot = contentEntry.file ?: return@forEach
                            if (!VfsUtil.isAncestor(contentRoot, virtualFile, false)) return@forEach
                            val alreadyRegistered = contentEntry.sourceFolders.any { sourceFolder ->
                                sourceFolder.file?.url == virtualFile.url
                            }
                            if (!alreadyRegistered) {
                                contentEntry.addSourceFolder(virtualFile, true)
                            }
                        }
                        model.commit()
                    } catch (error: Throwable) {
                        model.dispose()
                        throw error
                    }
                }
            }
        }
    }

    private fun ensureNotExcluded(generatedSourceRoot: VirtualFile) {
        ModuleManager.getInstance(project).modules.forEach { module ->
            val model = ModuleRootManager.getInstance(module).modifiableModel
            try {
                model.contentEntries.forEach { contentEntry ->
                    var current: VirtualFile? = generatedSourceRoot
                    while (current != null) {
                        removeExcludeFolder(contentEntry, current)
                        if (current.name == Constants.SDK_DIR) break
                        current = current.parent
                    }
                }
                model.commit()
            } catch (error: Throwable) {
                model.dispose()
                throw error
            }
        }
    }

    private fun removeExcludeFolder(contentEntry: ContentEntry, folder: VirtualFile) {
        if (contentEntry.excludeFolderUrls.contains(folder.url)) {
            contentEntry.removeExcludeFolder(folder.url)
        }
    }
}
