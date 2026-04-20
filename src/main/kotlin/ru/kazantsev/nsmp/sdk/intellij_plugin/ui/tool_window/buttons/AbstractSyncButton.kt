package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.buttons

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.BalloonNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.SrcRequestSelectDialog
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.SrcOptionsProvider
import java.awt.Dimension
import javax.swing.JButton

abstract class AbstractSyncButton(
    title: String,
    dialogTitle: String,
    protected val project: Project
) : JButton(title) {

    protected val syncUIAdapter: SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    protected val projectSettingsService: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()

    protected val balloonNotificationService: DialogNotificationService
        get() = project.service<DialogNotificationService>()

    protected val dialogNotificationService: DialogNotificationService
        get() = project.service<DialogNotificationService>()

    abstract val optionsProvider: SrcOptionsProvider

    abstract val action: (SrcRequestSelectState) -> Unit

    init {
        alignmentX = LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        addActionListener {
            if (!projectSettingsService.checkInstallationIsSpecified()) return@addActionListener
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