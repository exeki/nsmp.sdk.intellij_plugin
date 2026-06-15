package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.project.ProjectOpenInstallationAction
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

@Deprecated("Заменено гиперссылкой")
class OpenInstallationButton : AbstractButton(
    action = ProjectOpenInstallationAction(),
    presentation = Presentation().apply {
        text = MessageBundle.message("tool_window.button.open.installation")
        description = MessageBundle.message("tool_window.button.open.installation.description")
        isEnabledAndVisible = true
        icon = null
    }
)
