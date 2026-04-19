package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.components.buttons

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.SrcRequestSelectDialog
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.options_provider.SrcOptionsProvider
import java.awt.Dimension
import javax.swing.JButton

abstract class AbstractSyncButton(
    title: String,
    dialogTitle: String,
    protected val project: Project
) : JButton(title) {
    protected val syncUIAdapter = project.service<SyncUIAdapter>()

    protected val projectSettingsService = project.service<ProjectSettingsService>()

    protected val balloonNotificationService = project.service<BalloonNotificationService>()

    protected val dialogNotificationService = project.service<DialogNotificationService>()

    abstract val optionsProvider: SrcOptionsProvider

    abstract val action: (SrcRequestSelectState) -> Unit

    init {
        alignmentX = LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        addActionListener {
            if(!projectSettingsService.checkInstallationIsSpecified()) return@addActionListener
            val dialog = SrcRequestSelectDialog(
                title = dialogTitle,
                project = project,
                withForceCheckbox = false,
                optionsProvider = optionsProvider,
                action = action,
            )
            dialog.showAndGet()
        }
    }
}