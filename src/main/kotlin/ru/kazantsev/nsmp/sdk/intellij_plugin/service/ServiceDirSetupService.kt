package ru.kazantsev.nsmp.sdk.intellij_plugin.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import ru.kazantsev.nsmp.sdk.sources_sync.Constants
import java.nio.file.Files
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class ServiceDirSetupService(private val project: Project) {

    fun ensureServiceDirLayout() {
        ensureExcludedSubfoldersInModules()
        ensureGitIgnoreRule()
    }

    /** @deprecated use [ensureServiceDirLayout] */
    fun ensureExcludedAndGitIgnore() = ensureServiceDirLayout()

    private fun ensureExcludedSubfoldersInModules() {
        ApplicationManager.getApplication().invokeAndWait {
            WriteAction.run<RuntimeException> {
                ModuleManager.getInstance(project).modules.forEach { module ->
                    val model = ModuleRootManager.getInstance(module).modifiableModel
                    try {
                        model.contentEntries.forEach { contentEntry ->
                            val contentRoot = contentEntry.file ?: return@forEach
                            val serviceDir = VfsUtil.findRelativeFile(contentRoot, Constants.SDK_DIR) ?: return@forEach

                            removeExcludeFolder(contentEntry, serviceDir)

                            NsmpSdkLayout.INDEX_EXCLUDED_SUBDIRS.forEach { subDirPath ->
                                val subDir = findRelativePath(serviceDir, subDirPath) ?: return@forEach
                                if (contentEntry.excludeFolderUrls.none { it == subDir.url }) {
                                    contentEntry.addExcludeFolder(subDir)
                                }
                            }
                        }
                        model.commit()
                    } catch (t: Throwable) {
                        model.dispose()
                        throw t
                    }
                }
            }
        }
    }

    private fun removeExcludeFolder(contentEntry: com.intellij.openapi.roots.ContentEntry, folder: com.intellij.openapi.vfs.VirtualFile) {
        if (contentEntry.excludeFolderUrls.contains(folder.url)) {
            contentEntry.removeExcludeFolder(folder.url)
        }
    }

    private fun findRelativePath(
        base: com.intellij.openapi.vfs.VirtualFile,
        relativePath: String,
    ): com.intellij.openapi.vfs.VirtualFile? {
        var current: com.intellij.openapi.vfs.VirtualFile? = base
        for (segment in relativePath.split('/')) {
            current = current?.findChild(segment) ?: return null
        }
        return current
    }

    fun ensureGitIgnoreRule() {
        val basePath = project.basePath ?: return
        val gitIgnore = Path.of(basePath, ".gitignore")
        if (!Files.exists(gitIgnore)) return

        val rule = "${Constants.SDK_DIR}/"
        val lines = Files.readAllLines(gitIgnore)
        if (lines.any { it.trim() == rule || it.trim() == Constants.SDK_DIR }) return

        val suffix = if (lines.isEmpty()) rule else System.lineSeparator() + rule
        Files.writeString(
            gitIgnore,
            suffix,
            java.nio.file.StandardOpenOption.APPEND,
        )
    }
}
