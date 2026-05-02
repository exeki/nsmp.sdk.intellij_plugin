package ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.SyncService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_top_panel.TopPanelScrollPane
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class TopPanelService(private val project: Project) {

    private val syncService = project.service<SyncService>()
    private val pluginActivationService = project.service<PluginActivationService>()

    private val fileEditorManager = FileEditorManager.getInstance(project)

    companion object {
        val TOP_PANEL_FILE_URL_KEY = Key.create<String>("nsmp.sdk.groovy.sync.top.panel.file.url")
        val TOP_PANEL_KEY = Key.create<JComponent>("nsmp.sdk.groovy.sync.top.panel")
    }

    fun processEditor(editor: FileEditor, force: Boolean = false) {
        val file = editor.file
        val existing = editor.getUserData(TOP_PANEL_KEY)
        val existingFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)

        val wrongUrl = existingFileUrl != null && existingFileUrl != file.url
        val isSupportedFile = syncService.isSupportedFile(file)
        val pluginActive = pluginActivationService.pluginActive
        val needToInstall = pluginActive && isSupportedFile
        if (needToInstall) {
            if (existing == null) installTopPanel(editor)
            else if ((force || wrongUrl)) {
                removeTopPanel(editor)
                installTopPanel(editor)
            }
        } else if (existing != null) removeTopPanel(editor)
    }

    fun installTopPanel(editor: FileEditor) {
        val panel = TopPanelScrollPane(project, editor.file)
        fileEditorManager.addTopComponent(editor, panel)
        editor.putUserData(TOP_PANEL_KEY, panel)
        editor.putUserData(TOP_PANEL_FILE_URL_KEY, editor.file.url)

        Disposer.register(editor) { removeTopPanel(editor) }
    }

    fun removeTopPanel(editor: FileEditor) {
        val panel = editor.getUserData(TOP_PANEL_KEY) ?: return
        fileEditorManager.removeTopComponent(editor, panel)
        editor.putUserData(TOP_PANEL_KEY, null)
        editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
    }
}