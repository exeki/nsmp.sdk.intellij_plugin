package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.model

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
