package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.panels.ScrollableContainer
import java.awt.Dimension

class TopPanelScrollPane(
    project: Project,
    file: VirtualFile,
) : JBScrollPane(
    ScrollableContainer(file, project),
    VERTICAL_SCROLLBAR_NEVER,
    HORIZONTAL_SCROLLBAR_NEVER
) {
    val topPanel = ScrollableContainer(file, project)

    companion object {
        const val PANEL_HEIGHT = 35
        private const val SCROLLBAR_HEIGHT = 6
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
    }

    init {
        fun setWidth(width: Int) {
            preferredSize = Dimension(preferredSize.width, JBUI.scale(width))
            minimumSize = Dimension(0, JBUI.scale(width))
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(width))
        }
        isOpaque = false
        viewport.isOpaque = false
        val borderColor = JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground()
        border = JBUI.Borders.customLineBottom(borderColor)
        viewportBorder = JBUI.Borders.empty()
        horizontalScrollBar.border = JBUI.Borders.empty()
        horizontalScrollBar.preferredSize = Dimension(
            horizontalScrollBar.preferredSize.width,
            JBUI.scale(SCROLLBAR_HEIGHT)
        )
        addMouseWheelListener { event ->
            val maxValue = horizontalScrollBar.maximum - horizontalScrollBar.visibleAmount
            val nextValue =
                horizontalScrollBar.value + event.unitsToScroll * JBUI.scale(HORIZONTAL_SCROLL_INCREMENT)
            horizontalScrollBar.value = nextValue.coerceIn(horizontalScrollBar.minimum, maxValue)
            event.consume()
        }
        val updateHorizontalPolicy = {
            val viewportWidth = viewport.extentSize.width
            val contentWidth = topPanel.preferredSize.width
            if (viewportWidth in 1..<contentWidth) {
                horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
                setWidth(PANEL_HEIGHT + SCROLLBAR_HEIGHT)
            } else {
                horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
                setWidth(PANEL_HEIGHT)
            }
        }
        viewport.addChangeListener { updateHorizontalPolicy() }
        add(topPanel)
        updateHorizontalPolicy()
    }
}