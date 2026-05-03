package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class FilePushConfirmationDialog(project: Project) : DialogWrapper(project) {
    private val forceCheckBox = JBCheckBox(MessageBundle.message("sync.dialog.force"))

    val force: Boolean
        get() = forceCheckBox.isSelected

    init {
        title = MessageBundle.message("sync.command.push.file.confirmation.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return JPanel(BorderLayout(0, 8)).apply {
            add(JBLabel(MessageBundle.message("sync.command.push.file.confirmation.question")), BorderLayout.NORTH)
            add(forceCheckBox, BorderLayout.CENTER)
        }
    }
}
