package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import java.awt.Component
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

abstract class AbstractButton(
    title: String,
    protected val file: VirtualFile,
    protected val project: Project
) : AnAction(title), CustomComponentAction {

    val syncUIAdapter: SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    val balloonNotificationService: BalloonNotificationService
        get() = project.service<BalloonNotificationService>()

    val dialogNotificationService: DialogNotificationService
        get() = project.service<DialogNotificationService>()

    abstract fun compatibleWithFile() : Boolean

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            alignmentY = Component.CENTER_ALIGNMENT
            add(createButtonComponent(presentation, place).apply {
                alignmentY = Component.CENTER_ALIGNMENT
            })
        }
    }

    protected fun createButtonComponent(presentation: Presentation, place: String): JComponent {
        return ActionButtonWithText(this, presentation, place, Dimension(48, BUTTON_HEIGHT))
    }

    private companion object {
        const val BUTTON_HEIGHT = 20
    }
}
