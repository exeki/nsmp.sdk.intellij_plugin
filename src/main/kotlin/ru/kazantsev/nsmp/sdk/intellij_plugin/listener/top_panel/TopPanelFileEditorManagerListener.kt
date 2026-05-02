package ru.kazantsev.nsmp.sdk.intellij_plugin.listener.top_panel

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.TopPanelService

class TopPanelFileEditorManagerListener(project: Project) : FileEditorManagerListener, DumbAware {
    private val topPanelService = project.service<TopPanelService>()

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        source.getEditors(file).forEach { editor ->
            topPanelService.processEditor(editor,  false)
        }
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        event.manager.getEditors(file).forEach { editor ->
            topPanelService.processEditor(editor,  true)
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        source.getEditors(file).forEach { editor ->
            topPanelService.removeTopPanel(editor)
        }
    }
}