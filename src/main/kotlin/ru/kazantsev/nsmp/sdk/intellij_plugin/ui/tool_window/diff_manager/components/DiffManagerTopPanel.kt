package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Rectangle
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.Scrollable

class DiffManagerTopPanel : JPanel(BorderLayout()) {

    companion object {
        const val COMPONENT_HEIGHT = 28
        private const val SCROLLBAR_HEIGHT = 6
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
    }

    private val contentPanel = object : JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0)), Scrollable {
        override fun getScrollableTracksViewportWidth(): Boolean = false

        override fun getScrollableTracksViewportHeight(): Boolean = true

        override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

        override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
            JBUI.scale(HORIZONTAL_SCROLL_INCREMENT)

        override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
            visibleRect.width.coerceAtLeast(JBUI.scale(HORIZONTAL_SCROLL_INCREMENT))
    }

    private val scrollPane = JBScrollPane(contentPanel).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        border = JBUI.Borders.empty()
        isOpaque = false
        viewport.isOpaque = false
        horizontalScrollBar.border = JBUI.Borders.empty()
        horizontalScrollBar.preferredSize = Dimension(
            horizontalScrollBar.preferredSize.width,
            JBUI.scale(SCROLLBAR_HEIGHT),
        )
        viewport.addChangeListener { updateHorizontalScrollPolicy() }
    }

    val leftLabel = JLabel(MessageBundle.message("sync.command.diff.manager.compare.left"))
    val leftStateFilter = ComboBox(SrcState.entries.toTypedArray())
    val rightLabel = JLabel(MessageBundle.message("sync.command.diff.manager.compare.right"))
    val rightStateFilter = ComboBox(SrcState.entries.toTypedArray())
    val refreshButton = JButton(MessageBundle.message("sync.command.diff.manager.refresh"))
    val updateRemoteButton = JButton(MessageBundle.message("sync.command.diff.manager.remote.update"))
    val typeLabel = JLabel(MessageBundle.message("sync.command.diff.manager.table.type"))
    val typeFilterField = ComboBox(DiffTypeFilter.entries.toTypedArray())
    val codeLabel = JLabel(MessageBundle.message("sync.command.diff.manager.table.code"))
    val codeFilterField = JBTextField(18)

    init {
        leftStateFilter.renderer = stateRenderer()
        rightStateFilter.renderer = stateRenderer()
        typeFilterField.renderer = typeRenderer()
        leftStateFilter.selectedItem = SrcState.CURRENT
        rightStateFilter.selectedItem = SrcState.LAST_SYNC
        add(leftLabel)
        add(leftStateFilter)
        add(rightLabel)
        add(rightStateFilter)
        add(refreshButton)
        add(updateRemoteButton)
        add(typeLabel)
        add(typeFilterField)
        add(codeLabel)
        add(codeFilterField)
        add(scrollPane, BorderLayout.CENTER)
        updateHorizontalScrollPolicy()
    }

    private fun stateRenderer() = SimpleListCellRenderer.create<SrcState>("") { value ->
        value?.localizedName().orEmpty()
    }

    private fun typeRenderer() = SimpleListCellRenderer.create<DiffTypeFilter>("") { value ->
        value?.localizedName().orEmpty()
    }

    override fun add(component: Component): Component? {
        val comp = contentPanel.add(component)
        if (comp is JComponent) comp.apply {
            alignmentY = CENTER_ALIGNMENT
            preferredSize = Dimension(preferredSize.width, JBUI.scale(COMPONENT_HEIGHT))
            minimumSize = Dimension(0, JBUI.scale(COMPONENT_HEIGHT))
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(COMPONENT_HEIGHT))
        }
        return comp
    }

    private fun updateHorizontalScrollPolicy() {
        val viewportWidth = scrollPane.viewport.extentSize.width
        val contentWidth = contentPanel.preferredSize.width
        val needsHorizontalScroll = viewportWidth in 1..<contentWidth
        scrollPane.horizontalScrollBarPolicy = if (needsHorizontalScroll) {
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        } else {
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        }
        val height = JBUI.scale(if (needsHorizontalScroll) COMPONENT_HEIGHT + SCROLLBAR_HEIGHT else COMPONENT_HEIGHT)
        preferredSize = Dimension(preferredSize.width, height)
        minimumSize = Dimension(0, height)
        maximumSize = Dimension(Int.MAX_VALUE, height)
        revalidate()
        repaint()
    }
}
