package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model

/**
 * Модель выбранной опции для сохранения в настройках проекта
 */
class SelectedSrcOption {
    var code: String = ""
    var title: String = ""

    @Suppress("unused")
    constructor()

    constructor(code: String, title: String) {
        this.code = code
        this.title = title
    }
}
