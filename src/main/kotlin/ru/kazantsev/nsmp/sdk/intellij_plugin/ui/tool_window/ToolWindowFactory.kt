package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.init.GradleProjectInitializer
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.PullButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.PushButton
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons.SyncCheckButton
import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator

/**
 * Боковое меню справа
 */
class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(createInitGroup(project))
            add(Box.createVerticalStrut(10))
            add(createSourcesSyncGroup(project))
            add(Box.createVerticalGlue())
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createSourcesSyncGroup(project: Project): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(createGroupHeader(MessageBundle.message("group.sources.sync.title")))
            add(PullButton(project))
            add(Box.createVerticalStrut(8))
            add(SyncCheckButton(project))
            add(Box.createVerticalStrut(8))
            add(PushButton(project))
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
            add(createGroupHeader(MessageBundle.message("group.init.title")))
            add(statusLabel)
            add(Box.createVerticalStrut(8))
            add(JButton(MessageBundle.message("init.command.title")).apply {
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

    private fun executeInitProject(project: Project, statusLabel: JLabel) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            MessageBundle.message("init.command.title"),
            false
        ) {
            override fun run(indicator: ProgressIndicator) {
                runCatching {
                    GradleProjectInitializer.initialize(project)
                }.onSuccess { result ->
                    ApplicationManager.getApplication().invokeLater {
                        refreshProjectExplorer(project)
                        updateInitStatusLabel(project, statusLabel)
                        val message = if (result.changed)  MessageBundle.message("init.command.success")
                         else MessageBundle.message("init.command.already.initialized")
                        project.getService(DialogNotificationService::class.java).showInfo(
                            MessageBundle.message("init.command.title"),
                            message
                        )
                    }
                }.onFailure { error ->
                    ApplicationManager.getApplication().invokeLater {
                        project.getService(DialogNotificationService::class.java).showError(
                            MessageBundle.message("init.command.title"),
                            error.message ?: MessageBundle.message("sync.error.unknown"),
                        )
                    }
                }
            }
        })
    }

    private fun updateInitStatusLabel(project: Project, statusLabel: JLabel) {
        val status =
            if (GradleProjectInitializer.isProjectInitialized(project)) MessageBundle.message("init.status.initialized")
            else MessageBundle.message("init.status.not.initialized")
        statusLabel.text = MessageBundle.message("init.status.label", status)
    }

    private fun refreshProjectExplorer(project: Project) {
        VirtualFileManager.getInstance().asyncRefresh {
            ApplicationManager.getApplication().invokeLater {
                ProjectView.getInstance(project).refresh()
            }
        }
    }
}