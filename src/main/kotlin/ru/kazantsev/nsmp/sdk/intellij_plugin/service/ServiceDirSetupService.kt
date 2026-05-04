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

    fun ensureExcludedAndGitIgnore() {
        ensureExcludedInModules()
        ensureGitIgnoreRule()
    }

    fun ensureExcludedInModules() {
        ApplicationManager.getApplication().invokeAndWait {
            WriteAction.run<RuntimeException> {
                ModuleManager.getInstance(project).modules.forEach { module ->
                    val model = ModuleRootManager.getInstance(module).modifiableModel
                    try {
                        model.contentEntries.forEach { contentEntry ->
                            val contentRoot = contentEntry.file ?: return@forEach
                            val serviceDir = VfsUtil.findRelativeFile(contentRoot, Constants.SDK_DIR) ?: return@forEach
                            if (contentEntry.excludeFolderUrls.none { it == serviceDir.url }) {
                                contentEntry.addExcludeFolder(serviceDir)
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
            java.nio.file.StandardOpenOption.APPEND
        )
    }
}

