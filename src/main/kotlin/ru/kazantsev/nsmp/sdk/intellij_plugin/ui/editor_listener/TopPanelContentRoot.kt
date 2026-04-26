package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.*
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels.CollapsedPanelContent
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels.PanelContent
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteCollapsedContent
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteContextField
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.subcomponents.FileExecuteContextTitle
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFormat
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.Scrollable

class TopPanelContentRoot(
    private val file: VirtualFile,
    private val project: Project,
) : JPanel(BorderLayout()), Scrollable {

    private companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
        private const val PANEL_HEIGHT = 35
        private const val PANEL_VERTICAL_GAP = 4
        private const val PANEL_SIDE_GAP = 8
    }

    private val projectSettingsService=  project.service<ProjectSettingsService>()

    private var collapsed = projectSettingsService.isTopPanelCollapsed()

    private val logo = TopPanelLogo(::toggleCollapsed)

    private val contentPanel = PanelContent(project, file)

    private val collapsedPanel = CollapsedPanelContent(project, file)

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = JBUI.Borders.empty(PANEL_VERTICAL_GAP, PANEL_SIDE_GAP)
        isOpaque = false
        alignmentY = CENTER_ALIGNMENT
        preferredSize.height = JBUI.scale(PANEL_HEIGHT)
        add(logo)
        contentPanel
        add(contentPanel)
        add(collapsedPanel)
        applyCollapsedState()
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

    override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        return JBUI.scale(HORIZONTAL_SCROLL_INCREMENT)
    }

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        return visibleRect.width.coerceAtLeast(JBUI.scale(HORIZONTAL_SCROLL_INCREMENT))
    }

    override fun getScrollableTracksViewportWidth(): Boolean = false

    override fun getScrollableTracksViewportHeight(): Boolean = false
}
