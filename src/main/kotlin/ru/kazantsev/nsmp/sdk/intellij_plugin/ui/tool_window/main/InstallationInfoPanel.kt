package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectOpenInstallationAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.topics.SettingsTopic
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class InstallationInfoPanel(
    private val project: Project
) : JPanel(), Disposable {

    private val idLabel = JBLabel()
    private val hostLabel = JBLabel()
    private val openInstallationAction = ProjectOpenInstallationAction()
    private var hostLinkEnabled = false
    val messageBus = project.messageBus.connect(this)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.emptyLeft(8)
        isOpaque = false
        idLabel.foreground = UIUtil.getLabelForeground()
        hostLabel.foreground = UIUtil.getLabelForeground()
        add(idLabel)
        add(Box.createVerticalStrut(4))
        add(hostLabel)
        add(Box.createVerticalStrut(8))
        hostLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (hostLinkEnabled) openInstallationInBrowser()
            }
        })
        refresh()
        messageBus.subscribe(SettingsTopic.TOPIC, object : SettingsTopic {
            override fun settingsChanged() {
                refresh()
            }
        })
    }

    fun refresh() {
        val projectSettings = project.service<ProjectSettingsService>()
        if (!projectSettings.installationIsSpecified()) {
            idLabel.text = formatLine(MessageBundle.message("tool_window.main.installation.id"), "—")
            setHostPlain("—")
        } else {
            val connectorParams = projectSettings.connectorParams
            idLabel.text = formatLine(MessageBundle.message("tool_window.main.installation.id"), connectorParams.installationId)
            val url = "${connectorParams.scheme}://${connectorParams.host}/sd/admin"
            setHostLink(connectorParams.host, url)
        }
        revalidate()
        repaint()
    }

    private fun setHostPlain(host: String) {
        hostLinkEnabled = false
        hostLabel.text = formatLine(MessageBundle.message("tool_window.main.installation.host"), host)
        hostLabel.cursor = Cursor.getDefaultCursor()
        hostLabel.toolTipText = null
    }

    private fun setHostLink(host: String, url: String) {
        hostLinkEnabled = true
        val label = escapeHtml(MessageBundle.message("tool_window.main.installation.host"))
        hostLabel.text = "<html>$label: <a href=\"${escapeHtml(url)}\">${escapeHtml(host)}</a></html>"
        hostLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        hostLabel.toolTipText = url
    }

    private fun openInstallationInBrowser() {
        ActionManager.getInstance().tryToExecute(
            openInstallationAction,
            null,
            hostLabel,
            MainToolWindowFactory.ACTION_PLACE,
            true
        )
    }

    override fun getMaximumSize(): Dimension =
        Dimension(Int.MAX_VALUE, preferredSize.height)

    private fun formatLine(label: String, value: String): String = "$label: $value"

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    override fun dispose() {
        messageBus.dispose()
    }
}
