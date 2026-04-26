package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.TopPanelScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.Scrollable

class ScrollableContainer(
    file: VirtualFile,
    project: Project,
) : JPanel(BorderLayout()), Scrollable {

    private companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = false
        alignmentY = TOP_ALIGNMENT
        preferredSize.height = JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT)
        minimumSize = Dimension(0, JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT))
        maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(TopPanelScrollPane.PANEL_HEIGHT))
        add(TopPanelContentRoot(file, project))
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