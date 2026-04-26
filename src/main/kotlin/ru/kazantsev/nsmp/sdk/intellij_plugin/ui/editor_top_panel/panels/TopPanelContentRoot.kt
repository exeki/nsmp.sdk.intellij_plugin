package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.TopPanelScrollPane
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels.content.CollapsedPanelContent
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels.content.PanelContent
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.subcomponents.TopPanelLogo
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class TopPanelContentRoot(
    file: VirtualFile,
    project: Project,
) : JPanel(BorderLayout()) {

    private val projectSettingsService = project.service<ProjectSettingsService>()

    private var collapsed = projectSettingsService.isTopPanelCollapsed()

    private val logo = TopPanelLogo(::toggleCollapsed)

    private val contentPanel = PanelContent(project, file)

    private val collapsedPanel = CollapsedPanelContent(project, file)

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false
        border = JBUI.Borders.empty(0, 3)
        alignmentY = TOP_ALIGNMENT
        preferredSize.height = JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT)
        minimumSize = Dimension(0, JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT))
        maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT))
        applyCollapsedState()
        add(logo)
        add(contentPanel)
        add(collapsedPanel)
    }

    override fun add(comp: Component): Component? {
        if (comp is JComponent) comp.alignmentY = CENTER_ALIGNMENT
        return super.add(comp)
    }

    private fun toggleCollapsed() {
        collapsed = !collapsed
        projectSettingsService.setTopPanelCollapsed(collapsed)
        applyCollapsedState()
    }

    private fun applyCollapsedState() {
        contentPanel.isVisible = !collapsed
        collapsedPanel.isVisible = collapsed
        revalidate()
        repaint()
    }
}