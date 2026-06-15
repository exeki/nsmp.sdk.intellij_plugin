package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import javax.swing.Action
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class FileExecuteResultDialog(
    project: Project,
    private val fileName: String,
    private val contextFileRelativePath: String?,
    private val result: String,
) : DialogWrapper(project) {

    init {
        title = MessageBundle.message("sync.command.execute.file.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val screenBounds = screenBounds()
        val dialogWidth = (screenBounds.width * WIDTH_SCREEN_FRACTION).toInt()
        val maxContentHeight = (screenBounds.height * MAX_HEIGHT_SCREEN_FRACTION).toInt() - DIALOG_CHROME_HEIGHT

        val resultArea = JBTextArea(result).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font(Font.MONOSPACED, font.style, font.size)
            border = JBUI.Borders.empty()
            setSize(dialogWidth - JBUI.scale(24), Int.MAX_VALUE)
        }
        val contentHeight = resultArea.preferredSize.height
            .coerceIn(MIN_CONTENT_HEIGHT, maxContentHeight.coerceAtLeast(MIN_CONTENT_HEIGHT))

        val headerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.emptyBottom(JBUI.scale(8))
            add(JBLabel(MessageBundle.message("sync.command.execute.file.executed", fileName)).apply {
                alignmentX = JComponent.LEFT_ALIGNMENT
            })
            if (!contextFileRelativePath.isNullOrBlank()) {
                add(JBLabel(MessageBundle.message("sync.command.execute.context.title", contextFileRelativePath)).apply {
                    alignmentX = JComponent.LEFT_ALIGNMENT
                })
            }
            add(JBLabel(MessageBundle.message("sync.command.execute.file.result.title") + ":").apply {
                alignmentX = JComponent.LEFT_ALIGNMENT
                border = JBUI.Borders.emptyTop(JBUI.scale(8))
            })
        }

        val scrollPane = JBScrollPane(resultArea).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            preferredSize = Dimension(dialogWidth, contentHeight)
            border = JBUI.Borders.empty()
        }

        return JPanel(BorderLayout()).apply {
            preferredSize = Dimension(dialogWidth, contentHeight + headerPanel.preferredSize.height)
            add(headerPanel, BorderLayout.NORTH)
            add(scrollPane, BorderLayout.CENTER)
        }
    }

    override fun createActions(): Array<Action> = arrayOf(okAction)

    companion object {
        private const val WIDTH_SCREEN_FRACTION = 0.5
        private const val MAX_HEIGHT_SCREEN_FRACTION = 2.0 / 3.0
        private const val DIALOG_CHROME_HEIGHT = 140
        private const val MIN_CONTENT_HEIGHT = 120

        fun show(
            project: Project,
            fileName: String,
            contextFileRelativePath: String?,
            result: String,
        ) {
            FileExecuteResultDialog(project, fileName, contextFileRelativePath, result).show()
        }

        private fun screenBounds(): Rectangle =
            GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
    }
}
