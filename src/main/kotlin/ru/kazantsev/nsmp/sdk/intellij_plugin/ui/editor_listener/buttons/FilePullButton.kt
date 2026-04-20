package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle

class FilePullButton(
    file: VirtualFile,
    project: Project
) : AbstractButton(
    title = MessageBundle.message("sync.command.pull.short.title"),
    file = file,
    project = project
) {
    init {
        addActionListener {
            syncUIAdapter.pull(
                file = file,
                onSuccessCallback = { },
                onFailureCallback = { }
            )
        }
    }
}
