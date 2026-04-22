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
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Scrollable

class ScrollableTopPanelContent(
    private val file: VirtualFile,
    private val project: Project
) : JPanel(BorderLayout()), Scrollable {

    private companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
        private const val PANEL_VERTICAL_GAP = 4
        private const val PANEL_SIDE_GAP = 8
        private const val ACTION_PLACE = "NSMP.EditorTopPanel"
        private const val TOOLBAR_HEIGHT = 20
    }

    val buttons = listOf(
        FileExecuteButton(file, project),
        FileOpenInBrowserButton(file, project),
        FilePullButton(file, project),
        FileSyncCheckButton(file, project),
        FilePushButton(file, project)
    )

    val actionGroup = DefaultActionGroup().apply {
        buttons.filter { it.compatibleWithFile() }
            .forEachIndexed { index, button ->
                if (index > 0) addSeparator()
                add(button)
            }
    }

    val toolbar = ActionManager.getInstance()
        .createActionToolbar(ACTION_PLACE, actionGroup, true)
        .apply {
            targetComponent = this@ScrollableTopPanelContent
            setMiniMode(true)
            minimumButtonSize = JBUI.size(48, TOOLBAR_HEIGHT)
        }.component.apply {
            isOpaque = false
        }

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = JBUI.Borders.empty(PANEL_VERTICAL_GAP, PANEL_SIDE_GAP)
        isOpaque = false
        add(JLabel(Icons.ColoredLogo))
        add(toolbar)
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
