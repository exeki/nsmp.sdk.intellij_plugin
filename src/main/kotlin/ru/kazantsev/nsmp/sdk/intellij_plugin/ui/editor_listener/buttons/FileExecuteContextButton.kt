package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.editor_listener.buttons

/*
class FileExecuteContextButton(
    private val project: Project,
    private val root: () -> VirtualFile?,
    private val selectedFile: () -> VirtualFile?,
    private val onSelected: (VirtualFile) -> Unit,
    private val onMissingRoot: () -> Unit
) : AnAction() {
    private val presentations = mutableListOf<Presentation>()

    init {
        updateText()
    }

    override fun actionPerformed(event: AnActionEvent) {
        val root = root() ?: run {
            onMissingRoot()
            return
        }

        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle(MessageBundle.message("sync.command.execute.context.file.dialog.title"))
            .withRoots(root)
            .withFileFilter { it.extension == GROOVY_EXTENSION }

        val chosenFile = FileChooser.chooseFile(descriptor, project, selectedFile() ?: root) ?: return
        onSelected(chosenFile)
        updateText()
    }

    fun registerPresentation(presentation: Presentation): Presentation {
        presentations += presentation
        updateText(presentation)
        return presentation
    }

    fun updateText() {
        val fileName = selectedFile()?.name ?: MessageBundle.message("sync.command.execute.context.none")
        val text = MessageBundle.message("sync.command.execute.context.title", fileName)
        templatePresentation.text = text
        presentations.forEach { it.text = text }
    }

    private fun updateText(presentation: Presentation) {
        val fileName = selectedFile()?.name ?: MessageBundle.message("sync.command.execute.context.none")
        presentation.text = MessageBundle.message("sync.command.execute.context.title", fileName)
    }

    private companion object {
        const val GROOVY_EXTENSION = "groovy"
    }
}
 */