package ru.kazantsev.nsmp.sdk.intellij_plugin.services.ui

import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.TopPanelScrollPane
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class TopPanelService(private val project: Project) {

    companion object {
        val TOP_PANEL_FILE_URL_KEY = Key.create<String>("nsmp.sdk.groovy.sync.top.panel.file.url")
        val TOP_PANEL_KEY = Key.create<JComponent>("nsmp.sdk.groovy.sync.top.panel")
    }

    fun installTopPanel(manager: FileEditorManager, editor: FileEditor, file: VirtualFile, force: Boolean) {
        val existing = editor.getUserData(TOP_PANEL_KEY)
        val existingFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)
        if (!force && existing != null && existingFileUrl == file.url) return

        if (existing != null) removeTopPanel(manager, editor)

        val panel = TopPanelScrollPane(project, file)
        manager.addTopComponent(editor, panel)
        editor.putUserData(TOP_PANEL_KEY, panel)
        editor.putUserData(TOP_PANEL_FILE_URL_KEY, file.url)

        Disposer.register(editor) {
            val registeredPanel = editor.getUserData(TOP_PANEL_KEY) ?: return@register
            manager.removeTopComponent(editor, registeredPanel)
            editor.putUserData(TOP_PANEL_KEY, null)
            editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
        }
    }

    fun removeTopPanel(manager: FileEditorManager, editor: FileEditor) {
        val panel = editor.getUserData(TOP_PANEL_KEY) ?: return
        manager.removeTopComponent(editor, panel)
        editor.putUserData(TOP_PANEL_KEY, null)
        editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
    }
}