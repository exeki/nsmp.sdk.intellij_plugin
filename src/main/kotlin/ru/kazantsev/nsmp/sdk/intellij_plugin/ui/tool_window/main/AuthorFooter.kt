package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main

import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.JBLabel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class AuthorFooter : JPanel() {
    companion object {
        private const val GITHUB_URL = "https://github.com/exeki"
    }

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false

        val authorLabel = JBLabel(MessageBundle.message("ui.footer.author"))
        val githubLabel = JBLabel("<html><a href=\"$GITHUB_URL\"'>${MessageBundle.message("ui.footer.github")}</a></html>").apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = GITHUB_URL
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    BrowserUtil.browse(GITHUB_URL)
                }
            })
        }

        add(Box.createHorizontalGlue())
        add(authorLabel)
        add(Box.createHorizontalStrut(6))
        add(githubLabel)

        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }
}
