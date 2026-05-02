package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.main.buttons

import com.intellij.openapi.actionSystem.Presentation
import ru.kazantsev.nsmp.sdk.intellij_plugin.actions.global.GlobalOpenInstallationAction

class OpenInstallationButton : AbstractButton(
    action = GlobalOpenInstallationAction(),
    //TODO перевести на бандл
    presentation = Presentation().apply {
        text = "Go to the installation"
        description = "Open installation main admin page in browser"
        isEnabledAndVisible = true
        icon = null
    }
)