package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.service.NsdHttpServerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.topics.ServerStateTopic
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class ServerStatePanel(
    project: Project
) : JPanel(), Disposable {

    private val statusLabel = JBLabel().apply {
        foreground = UIUtil.getLabelForeground()
    }
    private val restartButton = createRestartButton()
    private val statusRow = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false
        alignmentX = LEFT_ALIGNMENT
        add(statusLabel)
        add(Box.createHorizontalStrut(JBUI.scale(4)))
        add(restartButton)
    }
    private val detailsLabel = JBLabel().apply {
        foreground = UIUtil.getLabelForeground()
    }
    private val messageBus = project.messageBus.connect(this)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.emptyLeft(8)
        isOpaque = false
        add(statusRow)
        add(Box.createVerticalStrut(4))
        add(detailsLabel)
        add(Box.createVerticalStrut(8))
        refresh()
        messageBus.subscribe(ServerStateTopic.TOPIC, object : ServerStateTopic {
            override fun serverStarted(port: Int) {
                showRunning(port)
            }

            override fun serverStopped() {
                showStopped()
            }

            override fun serverStartError(e: Exception) {
                showError(e)
            }
        })
    }

    private fun refresh() {
        val port = AppSettingsService.getInstance().serverPort
        val serverService = ApplicationManager.getApplication().service<NsdHttpServerService>()
        when {
            port == 0 -> showDisabled()
            serverService.isRunning() -> showRunning(port)
            serverService.lastStartError() != null -> showError(serverService.lastStartError()!!)
            else -> showStopped()
        }
    }

    private fun showRunning(port: Int) {
        statusLabel.text = formatLine(
            MessageBundle.message("tool_window.main.server.status"),
            MessageBundle.message("tool_window.main.server.status.running")
        )
        detailsLabel.text = formatLine(MessageBundle.message("tool_window.main.server.port"), port.toString())
        detailsLabel.isVisible = true
        revalidate()
        repaint()
    }

    private fun showStopped() {
        statusLabel.text = formatLine(
            MessageBundle.message("tool_window.main.server.status"),
            MessageBundle.message("tool_window.main.server.status.stopped")
        )
        detailsLabel.isVisible = false
        revalidate()
        repaint()
    }

    private fun showDisabled() {
        statusLabel.text = formatLine(
            MessageBundle.message("tool_window.main.server.status"),
            MessageBundle.message("tool_window.main.server.status.disabled")
        )
        detailsLabel.isVisible = false
        revalidate()
        repaint()
    }

    private fun showError(error: Exception) {
        statusLabel.text = formatLine(
            MessageBundle.message("tool_window.main.server.status"),
            MessageBundle.message("tool_window.main.server.status.error")
        )
        detailsLabel.text = formatLine(
            MessageBundle.message("tool_window.main.server.error"),
            error.message ?: error.javaClass.simpleName
        )
        detailsLabel.isVisible = true
        revalidate()
        repaint()
    }

    override fun getMaximumSize(): Dimension =
        Dimension(Int.MAX_VALUE, preferredSize.height)

    override fun dispose() {
        messageBus.dispose()
    }

    private fun formatLine(label: String, value: String): String = "$label: $value"

    private fun createRestartButton(): ActionButton {
        val action = object : AnAction(
            MessageBundle.message("tool_window.main.server.restart"),
            MessageBundle.message("tool_window.main.server.restart"),
            AllIcons.Actions.Restart
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                ApplicationManager.getApplication().service<NsdHttpServerService>().restart()
            }
        }
        val lineHeight = statusLabel.getFontMetrics(statusLabel.font).height.coerceAtLeast(JBUI.scale(16))
        val buttonSize = JBUI.size(lineHeight, lineHeight)
        return ActionButton(
            action,
            action.templatePresentation,
            MainToolWindowFactory.ACTION_PLACE,
            buttonSize
        ).apply {
            alignmentY = CENTER_ALIGNMENT
            preferredSize = buttonSize
            maximumSize = buttonSize
        }
    }
}
