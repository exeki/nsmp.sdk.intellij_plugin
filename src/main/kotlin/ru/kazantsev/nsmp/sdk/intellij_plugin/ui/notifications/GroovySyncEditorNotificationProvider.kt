package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.notifications

import com.intellij.ide.projectView.ProjectView
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SrcCommandType
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SrcSyncProjectService
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import java.util.function.Function
import javax.swing.JComponent

class GroovySyncEditorNotificationProvider : EditorNotificationProvider {
    private val notifications by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("NSMP SDK Notifications")
    }

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<in FileEditor, out JComponent?>? {
        if (!isGroovyFile(file)) return null

        return Function { editor ->
            EditorNotificationPanel(editor).apply {
                text = MyMessageBundle.message("sync.editor.banner.title")
                createActionLabel(commandTitle(SrcCommandType.PULL)) {
                    executeCommand(project, file, SrcCommandType.PULL)
                }
                createActionLabel(commandTitle(SrcCommandType.SYNC_CHECK)) {
                    executeCommand(project, file, SrcCommandType.SYNC_CHECK)
                }
                createActionLabel(commandTitle(SrcCommandType.PUSH)) {
                    executeCommand(project, file, SrcCommandType.PUSH)
                }
            }
        }
    }

    private fun executeCommand(project: Project, file: VirtualFile, commandType: SrcCommandType) {
        val request = SrcRequest(
            modules = emptyList(),
            allModules = false,
            scripts = listOf(resolveScriptCode(file)),
            allScripts = false,
            advImports = emptyList(),
            allAdvImports = false
        )

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, commandTitle(commandType), false) {
            override fun run(indicator: ProgressIndicator) {
                val syncService = project.getService(SrcSyncProjectService::class.java)
                runCatching {
                    when (commandType) {
                        SrcCommandType.PULL -> {
                            val result = syncService.pull(request)
                            MyMessageBundle.message(
                                "sync.command.pull.success",
                                result.scripts.size,
                                result.modules.size,
                                result.advImports.size
                            )
                        }

                        SrcCommandType.SYNC_CHECK -> {
                            val result = syncService.syncCheck(request)
                            MyMessageBundle.message(
                                "sync.command.sync.check.success",
                                result.scripts.size,
                                result.modules.size,
                                result.advImports.size
                            )
                        }

                        SrcCommandType.PUSH -> {
                            val result = syncService.push(request)
                            MyMessageBundle.message(
                                "sync.command.push.success",
                                result.scripts.size,
                                result.modules.size,
                                result.advImports.size
                            )
                        }
                    }
                }.onSuccess { message ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer(project)
                        notifications.createNotification(
                            commandTitle(commandType),
                            message,
                            NotificationType.INFORMATION
                        ).notify(project)
                    }
                }.onFailure { error ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer(project)
                        notifications.createNotification(
                            commandTitle(commandType),
                            error.message ?: MyMessageBundle.message("sync.error.unknown"),
                            NotificationType.ERROR
                        ).notify(project)
                    }
                }
            }
        })
    }

    private fun isGroovyFile(file: VirtualFile): Boolean {
        return file.extension.equals("groovy", ignoreCase = true)
    }

    private fun resolveScriptCode(file: VirtualFile): String {
        return file.nameWithoutExtension.ifBlank { file.name }
    }

    private fun commandTitle(commandType: SrcCommandType): String {
        return when (commandType) {
            SrcCommandType.PULL -> MyMessageBundle.message("sync.command.pull.title")
            SrcCommandType.SYNC_CHECK -> MyMessageBundle.message("sync.command.sync.check.title")
            SrcCommandType.PUSH -> MyMessageBundle.message("sync.command.push.title")
        }
    }

    private fun refreshProjectExplorer(project: Project) {
        VirtualFileManager.getInstance().asyncRefresh {
            ApplicationManager.getApplication().invokeLater {
                ProjectView.getInstance(project).refresh()
            }
        }
    }
}

