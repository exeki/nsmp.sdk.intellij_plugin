package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.Icons
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Scrollable

class ScrollableTopPanelContent(
    private val file: VirtualFile,
    private val project: Project
) : JPanel(BorderLayout()), Scrollable {

    private companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 6
        private const val PANEL_VERTICAL_GAP = 4
        private const val PANEL_SIDE_GAP = 8
        private const val ACTION_PLACE = "NSMP.EditorTopPanel"
        private const val TOOLBAR_HEIGHT = 20
    }

    val actionGroup = DefaultActionGroup().apply {
        add(FileExecuteButton(file, project))
        addSeparator()
        add(FileOpenInBrowserButton(file, project))
        addSeparator()
        add(FilePullButton(file, project))
        addSeparator()
        add(FileSyncCheckButton(file, project))
        addSeparator()
        add(FilePushButton(file, project))
    }

    val toolbar = ActionManager.getInstance()
        .createActionToolbar(ACTION_PLACE, actionGroup, true)
        .apply {
            targetComponent = this@ScrollableTopPanelContent
            setMiniMode(true)
            minimumButtonSize = Dimension(48, TOOLBAR_HEIGHT)
        }.component.apply {
            isOpaque = false
        }

    init {
        border = JBUI.Borders.empty(PANEL_VERTICAL_GAP, PANEL_SIDE_GAP)
        isOpaque = false
        add(JLabel(Icons.MyIcon), BorderLayout.WEST)
        add(toolbar)
    }


    override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int = 0

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int {
        return HORIZONTAL_SCROLL_INCREMENT
    }

    override fun getScrollableTracksViewportWidth(): Boolean = false

    override fun getScrollableTracksViewportHeight(): Boolean = true
}
