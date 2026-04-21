package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.Icons
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels.FileEditorLeftActionsPanel
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.panels.FileEditorRightActionsPanel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class FileEditorManagerListener(private val project: Project) : FileEditorManagerListener, DumbAware {
    private val syncUIAdapter: SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (!syncUIAdapter.isSupportedFile(file)) return
        source.getEditors(file).forEach { editor ->
            installTopPanel(source, editor, file)
        }
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        if (!syncUIAdapter.isSupportedFile(file)) return
        event.manager.getEditors(file).forEach { editor ->
            installTopPanel(event.manager, editor, file)
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        source.allEditors.forEach { editor ->
            val panel = editor.getUserData(TOP_PANEL_KEY) ?: return@forEach
            val panelFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)
            if (panelFileUrl == file.url) {
                source.removeTopComponent(editor, panel)
                editor.putUserData(TOP_PANEL_KEY, null)
                editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
            }
        }
    }

    private fun installTopPanel(manager: FileEditorManager, editor: FileEditor, file: VirtualFile) {
        val existing = editor.getUserData(TOP_PANEL_KEY)
        val existingFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)
        if (existing != null && existingFileUrl == file.url) return

        if (existing != null) manager.removeTopComponent(editor, existing)

        val panel = createTopPanel(file)
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

    private fun createTopPanel(file: VirtualFile): JPanel {
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(PANEL_VERTICAL_GAP, PANEL_SIDE_GAP)
            isOpaque = false
            add(
                JLabel(Icons.MyIcon),
                BorderLayout.WEST
            )
            add(
                JPanel(BorderLayout()).apply {
                    isOpaque = false
                    add(FileEditorLeftActionsPanel(file, project), BorderLayout.WEST)
                    add(FileEditorRightActionsPanel(file, project), BorderLayout.EAST)
                }
            )
        }
    }

    private companion object {
        private const val PANEL_VERTICAL_GAP = 4
        private const val PANEL_SIDE_GAP = 8
        private val TOP_PANEL_KEY = Key.create<JComponent>("nsmp.sdk.groovy.sync.top.panel")
        private val TOP_PANEL_FILE_URL_KEY = Key.create<String>("nsmp.sdk.groovy.sync.top.panel.file.url")
    }
}
