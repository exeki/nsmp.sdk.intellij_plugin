package ru.kazantsev.nsmp.sdk.intellij_plugin.listener.top_panel

/*
class TopPanelFileEditorManagerListenerOld(private val project: Project) : FileEditorManagerListener, DumbAware {
    private val syncService = project.service<SyncService>()
    private val pluginActivationService = project.service<PluginActivationService>()

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (!syncService.isSupportedFile(file)) return
        source.getEditors(file).forEach { editor ->
            installTopPanel(source, editor, file, force = false)
        }
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        if (!syncService.isSupportedFile(file)) return
        event.manager.getEditors(file).forEach { editor ->
            installTopPanel(event.manager, editor, file, force = true)
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        source.allEditors.forEach { editor ->
            val panel = editor.getUserData(TopPanelService.TOP_PANEL_KEY) ?: return@forEach
            val panelFileUrl = editor.getUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY)
            if (panelFileUrl == file.url) {
                source.removeTopComponent(editor, panel)
                editor.putUserData(TopPanelService.TOP_PANEL_KEY, null)
                editor.putUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY, null)
            }
        }
    }

    private fun installTopPanel(manager: FileEditorManager, editor: FileEditor, file: VirtualFile, force: Boolean) {
        val existing = editor.getUserData(TopPanelService.TOP_PANEL_KEY)
        val existingFileUrl = editor.getUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY)
        if (!force && existing != null && existingFileUrl == file.url) return

        if (existing != null) removeTopPanel(manager, editor)

        val panel = TopPanelScrollPane(project, file)
        manager.addTopComponent(editor, panel)
        editor.putUserData(TopPanelService.TOP_PANEL_KEY, panel)
        editor.putUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY, file.url)

        Disposer.register(editor) {
            val registeredPanel = editor.getUserData(TopPanelService.TOP_PANEL_KEY) ?: return@register
            manager.removeTopComponent(editor, registeredPanel)
            editor.putUserData(TopPanelService.TOP_PANEL_KEY, null)
            editor.putUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY, null)
        }
    }

    private fun removeTopPanel(manager: FileEditorManager, editor: FileEditor) {
        val panel = editor.getUserData(TopPanelService.TOP_PANEL_KEY) ?: return
        manager.removeTopComponent(editor, panel)
        editor.putUserData(TopPanelService.TOP_PANEL_KEY, null)
        editor.putUserData(TopPanelService.TOP_PANEL_FILE_URL_KEY, null)
    }

}

 */