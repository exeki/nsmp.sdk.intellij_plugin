package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons.old

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import java.awt.Component
import java.awt.Container
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

@Deprecated("old")
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

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return createPanel().apply {
            add(createButtonComponent(presentation, place))
            alignmentY = Component.CENTER_ALIGNMENT
        }
    }

    fun createCustomComponent(): JComponent {
        return createCustomComponent(templatePresentation.clone(), ACTION_PLACE)
    }

    protected fun createPanel(): JPanel {
        return JPanel().apply {
            preferredSize.height = JBUI.scale(PANEL_HEIGHT)
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            alignmentY = Component.CENTER_ALIGNMENT
        }
    }

    protected fun createButtonComponent(presentation: Presentation, place: String): JComponent {
        return ActionButtonWithText(this, presentation, place, JBUI.size(48, BUTTON_HEIGHT)).apply {
            alignmentY = Component.CENTER_ALIGNMENT
        }
    }

    private companion object {
        private const val ACTION_PLACE = "NSMP.EditorTopPanel"
        const val BUTTON_HEIGHT = 20
        const val PANEL_HEIGHT = 30
    }
}