package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectSyncCheckAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class CheckButton : AbstractButton(
    action = ProjectSyncCheckAction(),
    presentation = Presentation().apply {
        text = MessageBundle.message("tool_window.button.sync.check")
        description = MessageBundle.message("tool_window.button.sync.check.description")
        isEnabledAndVisible = true
        icon = null
    }
)
