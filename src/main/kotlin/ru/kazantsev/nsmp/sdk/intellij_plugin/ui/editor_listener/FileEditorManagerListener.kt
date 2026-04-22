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
import com.intellij.ui.components.JBScrollPane
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants

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

    private fun createTopPanel(file: VirtualFile): JComponent {
        val topPanel = ScrollableTopPanelContent(file, project)

        return JBScrollPane(
            topPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        ).apply {
            isOpaque = false
            viewport.isOpaque = false
            border = JBUI.Borders.empty()
            horizontalScrollBar.preferredSize = Dimension(
                horizontalScrollBar.preferredSize.width,
                JBUI.scale(SCROLLBAR_HEIGHT)
            )
            addMouseWheelListener { event ->
                val maxValue = horizontalScrollBar.maximum - horizontalScrollBar.visibleAmount
                val nextValue = horizontalScrollBar.value + event.unitsToScroll * JBUI.scale(HORIZONTAL_SCROLL_INCREMENT)
                horizontalScrollBar.value = nextValue.coerceIn(horizontalScrollBar.minimum, maxValue)
                event.consume()
            }
            fun updateHeight() {
                val overflow = topPanel.preferredSize.width > viewport.extentSize.width
                val height = JBUI.scale(TOOLBAR_HEIGHT) + if (overflow) JBUI.scale(SCROLLBAR_HEIGHT) else 0
                preferredSize = Dimension(0, height)
                minimumSize = Dimension(0, height)
                horizontalScrollBarPolicy = if (overflow) ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
                else ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                revalidate()
                repaint()
            }
            horizontalScrollBar.model.addChangeListener {
                updateHeight()
            }
            viewport.addChangeListener {
                updateHeight()
            }
            updateHeight()
        }
    }

    companion object {
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
        private const val SCROLLBAR_HEIGHT = 6
        private const val TOOLBAR_HEIGHT = 33
        private val TOP_PANEL_KEY = Key.create<JComponent>("nsmp.sdk.groovy.sync.top.panel")
        private val TOP_PANEL_FILE_URL_KEY = Key.create<String>("nsmp.sdk.groovy.sync.top.panel.file.url")
    }
}
