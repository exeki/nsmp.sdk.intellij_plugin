package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import com.intellij.ide.projectView.ProjectView
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.init.GradleProjectInitializer
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.model.SrcRequestInputState
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SrcCommandType
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SrcSyncProjectService
import java.awt.Component
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator

class ToolWindowFactory : ToolWindowFactory {
    private val notifications by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("NSMP SDK Notifications")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(createInitGroup(project))
            add(Box.createVerticalStrut(10))
            add(createSourcesSyncGroup(project))
            add(Box.createVerticalGlue())
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createCommandButton(project: Project, commandType: SrcCommandType): JButton {
        return JButton(commandTitle(commandType)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            val buttonHeight = preferredSize.height
            maximumSize = Dimension(Int.MAX_VALUE, buttonHeight)
            addActionListener { executeCommand(project, commandType) }
        }
    }

    private fun createSourcesSyncGroup(project: Project): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(createGroupHeader(MyMessageBundle.message("group.sources.sync.title")))

            add(createCommandButton(project, SrcCommandType.PULL))
            add(Box.createVerticalStrut(8))
            add(createCommandButton(project, SrcCommandType.SYNC_CHECK))
            add(Box.createVerticalStrut(8))
            add(createCommandButton(project, SrcCommandType.PUSH))
        }
    }

    private fun createInitGroup(project: Project): JPanel {
        val statusLabel = JLabel().apply {
            alignmentX = Component.LEFT_ALIGNMENT
        }

        updateInitStatusLabel(project, statusLabel)

        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(createGroupHeader(MyMessageBundle.message("group.init.title")))
            add(statusLabel)
            add(Box.createVerticalStrut(8))
            add(JButton(MyMessageBundle.message("init.command.title")).apply {
                alignmentX = Component.LEFT_ALIGNMENT
                val buttonHeight = preferredSize.height
                maximumSize = Dimension(Int.MAX_VALUE, buttonHeight)
                addActionListener { executeInitProject(project, statusLabel) }
            })
        }
    }

    private fun createGroupHeader(title: String): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(JSeparator().apply {
                alignmentX = Component.LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, 1)
            })
            add(Box.createVerticalStrut(6))
            add(JLabel(title).apply { alignmentX = Component.LEFT_ALIGNMENT })
            add(Box.createVerticalStrut(8))
        }
    }

    private fun executeCommand(project: Project, commandType: SrcCommandType) {
        val projectSettings = ProjectSettingsService.getInstance(project)
        val initialInput = getStoredInput(projectSettings, commandType)

        val dialog = SrcRequestDialog(
            project = project,
            titleText = commandTitle(commandType),
            initialState = initialInput
        )
        if (!dialog.showAndGet()) return

        val updatedInput = dialog.getInputState()
        saveStoredInput(projectSettings, commandType, updatedInput)
        val request = dialog.getRequest()

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

    private fun executeInitProject(project: Project, statusLabel: JLabel) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            MyMessageBundle.message("init.command.title"),
            false
        ) {
            override fun run(indicator: ProgressIndicator) {
                runCatching {
                    GradleProjectInitializer.initialize(project)
                }.onSuccess { result ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer(project)
                        updateInitStatusLabel(project, statusLabel)
                        val message = if (result.changed) {
                            MyMessageBundle.message("init.command.success")
                        } else {
                            MyMessageBundle.message("init.command.already.initialized")
                        }
                        Messages.showInfoMessage(project, message, MyMessageBundle.message("init.command.title"))
                    }
                }.onFailure { error ->
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            project,
                            error.message ?: MyMessageBundle.message("sync.error.unknown"),
                            MyMessageBundle.message("init.command.title")
                        )
                    }
                }
            }
        })
    }

    private fun updateInitStatusLabel(project: Project, statusLabel: JLabel) {
        val status = if (GradleProjectInitializer.isProjectInitialized(project)) {
            MyMessageBundle.message("init.status.initialized")
        } else {
            MyMessageBundle.message("init.status.not.initialized")
        }
        statusLabel.text = MyMessageBundle.message("init.status.label", status)
    }

    private fun refreshProjectExplorer(project: Project) {
        VirtualFileManager.getInstance().asyncRefresh {
            ApplicationManager.getApplication().invokeLater {
                ProjectView.getInstance(project).refresh()
            }
        }
    }

    private fun commandTitle(commandType: SrcCommandType): String {
        return when (commandType) {
            SrcCommandType.PULL -> MyMessageBundle.message("sync.command.pull.title")
            SrcCommandType.SYNC_CHECK -> MyMessageBundle.message("sync.command.sync.check.title")
            SrcCommandType.PUSH -> MyMessageBundle.message("sync.command.push.title")
        }
    }

    private fun getStoredInput(
        settings: ProjectSettingsService,
        commandType: SrcCommandType
    ) = when (commandType) {
        SrcCommandType.PULL -> settings.pullRequestInput
        SrcCommandType.SYNC_CHECK -> settings.syncCheckRequestInput
        SrcCommandType.PUSH -> settings.pushRequestInput
    }

    private fun saveStoredInput(
        settings: ProjectSettingsService,
        commandType: SrcCommandType,
        input: SrcRequestInputState
    ) {
        when (commandType) {
            SrcCommandType.PULL -> settings.pullRequestInput = input
            SrcCommandType.SYNC_CHECK -> settings.syncCheckRequestInput = input
            SrcCommandType.PUSH -> settings.pushRequestInput = input
        }
    }
}
