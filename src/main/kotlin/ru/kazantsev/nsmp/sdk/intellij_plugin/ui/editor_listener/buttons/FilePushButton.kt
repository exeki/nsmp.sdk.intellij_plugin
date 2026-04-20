package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle

class FilePushButton(
    file: VirtualFile,
    project: Project,
    private val forceProvider: () -> Boolean
) : AbstractButton(
    title = MessageBundle.message("sync.command.push.short.title"),
    file = file,
    project = project
) {
    init {
        addActionListener {
            syncUIAdapter.push(
                file = file,
                force = forceProvider(),
                onSuccessCallback = { },
                onFailureCallback = { }
            )
        }
    }
}
