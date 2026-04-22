package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener

import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.Icons
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel

class TopPanelLogo(
    private val onToggle: () -> Unit
) : JLabel(Icons.ColoredLogo) {
    private var hovered = false

    init {
        isFocusable = true
        isOpaque = false
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        border = JBUI.Borders.empty(LOGO_FOCUS_VERTICAL_GAP, LOGO_FOCUS_HORIZONTAL_GAP)
        toolTipText = "Toggle NSMP SDK panel"
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(event: MouseEvent) {
                hovered = true
                requestFocusInWindow()
                repaint()
            }

            override fun mouseExited(event: MouseEvent) {
                hovered = false
                repaint()
            }

            override fun mouseClicked(event: MouseEvent) {
                requestFocusInWindow()
                onToggle()
            }
        })
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                if (event.keyCode == KeyEvent.VK_ENTER || event.keyCode == KeyEvent.VK_SPACE) {
                    onToggle()
                    event.consume()
                }
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        if (hovered) {
            val g2 = g.create() as Graphics2D
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = JBUI.CurrentTheme.ActionButton.hoverBackground()
                g2.fillRoundRect(0, 0, width, height, JBUI.scale(LOGO_FOCUS_ARC), JBUI.scale(LOGO_FOCUS_ARC))
            } finally {
                g2.dispose()
            }
        }
        super.paintComponent(g)
    }

    private companion object {
        private const val LOGO_FOCUS_VERTICAL_GAP = 3
        private const val LOGO_FOCUS_HORIZONTAL_GAP = 4
        private const val LOGO_FOCUS_ARC = 4
    }
}
