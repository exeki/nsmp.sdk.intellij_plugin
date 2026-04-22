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
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.util.ui.JBUI
import com.intellij.ui.components.JBScrollPane
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants

class FileEditorManagerListener(private val project: Project) : FileEditorManagerListener, DumbAware {
    private val syncUIAdapter: SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    init {
        project.messageBus.connect(project).subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    refreshPanelsForChangedFiles(events)
                }
            }
        )
    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (!syncUIAdapter.isSupportedFile(file)) return
        source.getEditors(file).forEach { editor ->
            installTopPanel(source, editor, file, force = false)
        }
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        if (!syncUIAdapter.isSupportedFile(file)) return
        event.manager.getEditors(file).forEach { editor ->
            installTopPanel(event.manager, editor, file, force = true)
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

    private fun refreshPanelsForChangedFiles(events: List<VFileEvent>) {
        val affectedUrls = events
            .filter(::isPanelRefreshEvent)
            .flatMap(::affectedUrls)
            .toSet()
        if (affectedUrls.isEmpty()) return

        val manager = FileEditorManager.getInstance(project)
        manager.allEditors.forEach { editor ->
            val fileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY) ?: return@forEach
            if (fileUrl !in affectedUrls) return@forEach

            val file = editor.file ?: return@forEach
            if (!syncUIAdapter.isSupportedFile(file)) {
                removeTopPanel(manager, editor)
                return@forEach
            }
            installTopPanel(manager, editor, file, force = true)
        }
    }

    private fun isPanelRefreshEvent(event: VFileEvent): Boolean {
        return event is VFileContentChangeEvent ||
            event is VFileMoveEvent ||
            event is VFilePropertyChangeEvent && event.propertyName == VirtualFile.PROP_NAME
    }

    private fun affectedUrls(event: VFileEvent): List<String> {
        return when (event) {
            is VFileMoveEvent -> listOf(event.oldParent.url + "/" + event.file.name, event.file.url)
            is VFilePropertyChangeEvent -> {
                val oldName = event.oldValue as? String
                if (event.propertyName == VirtualFile.PROP_NAME && oldName != null) {
                    listOf(event.file.parent.url + "/" + oldName, event.file.url)
                } else {
                    listOf(event.file.url)
                }
            }
            else -> event.file?.url?.let(::listOf).orEmpty()
        }
    }

    private fun installTopPanel(manager: FileEditorManager, editor: FileEditor, file: VirtualFile, force: Boolean) {
        val existing = editor.getUserData(TOP_PANEL_KEY)
        val existingFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)
        if (!force && existing != null && existingFileUrl == file.url) return

        if (existing != null) removeTopPanel(manager, editor)

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

    private fun removeTopPanel(manager: FileEditorManager, editor: FileEditor) {
        val panel = editor.getUserData(TOP_PANEL_KEY) ?: return
        manager.removeTopComponent(editor, panel)
        editor.putUserData(TOP_PANEL_KEY, null)
        editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
    }

    private fun createTopPanel(file: VirtualFile): JComponent {
        val topPanel = TopPanelContent(file, project)

        return JBScrollPane(
            topPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        ).apply {
            isOpaque = false
            viewport.isOpaque = false
            border = JBUI.Borders.empty()
            preferredSize = Dimension(0, JBUI.scale(SCROLL_PANEL_HEIGHT + SCROLLBAR_HEIGHT))
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
        }
    }

    companion object {
        private const val SCROLL_PANEL_HEIGHT = 30
        private const val HORIZONTAL_SCROLL_INCREMENT = 24
        private const val SCROLLBAR_HEIGHT = 6
        private val TOP_PANEL_KEY = Key.create<JComponent>("nsmp.sdk.groovy.sync.top.panel")
        private val TOP_PANEL_FILE_URL_KEY = Key.create<String>("nsmp.sdk.groovy.sync.top.panel.file.url")
    }
}
