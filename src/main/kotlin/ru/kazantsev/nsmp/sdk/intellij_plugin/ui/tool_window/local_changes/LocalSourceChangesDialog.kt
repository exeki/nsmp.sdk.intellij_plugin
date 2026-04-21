package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.local_changes

import com.intellij.diff.DiffManager
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Action
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class LocalSourceChangesDialog(
    private val project: Project
) : DialogWrapper(project) {
    private val changes = collectChanges()
    private val listModel = DefaultListModel<LocalFileChange>().apply {
        changes.forEach(::addElement)
    }
    private val changesList = JList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = LocalSourceChangesListCellRenderer()
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (event.clickCount == 2) showSelectedDiff()
            }
        })
    }

    init {
        title = MessageBundle.message("sync.command.local.changes.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            preferredSize = Dimension(720, 420)
            add(JBScrollPane(changesList), BorderLayout.CENTER)
        }
    }

    override fun createActions(): Array<Action> {
        return arrayOf(openDiffAction, cancelAction)
    }

    private val openDiffAction = object : DialogWrapperAction(
        MessageBundle.message("sync.command.local.changes.open.diff")
    ) {
        override fun doAction(e: java.awt.event.ActionEvent?) {
            showSelectedDiff()
        }
    }

    private fun showSelectedDiff() {
        val change = changesList.selectedValue ?: return
        val factory = DiffContentFactory.getInstance()
        val beforeText = change.beforePath?.takeIf { Files.exists(it) }?.let { Files.readString(it) }.orEmpty()
        val afterText = change.afterPath?.takeIf { Files.exists(it) }?.let { Files.readString(it) }.orEmpty()

        val request = SimpleDiffRequest(
            change.title,
            factory.create(beforeText),
            factory.create(afterText),
            MessageBundle.message("sync.command.local.changes.before"),
            MessageBundle.message("sync.command.local.changes.after")
        )
        DiffManager.getInstance().showDiff(project, request)
    }

    private fun collectChanges(): List<LocalFileChange> {
        val basePath = project.basePath ?: return emptyList()
        val projectRoot = Path.of(basePath)
        return listOf(
            DirectoryPair(
                name = MessageBundle.message("sync.command.local.changes.scripts"),
                beforeDir = projectRoot.resolve(".nsmp_sdk/last_sync_src/scripts"),
                afterDir = projectRoot.resolve("src/main/scripts")
            ),
            DirectoryPair(
                name = MessageBundle.message("sync.command.local.changes.modules"),
                beforeDir = projectRoot.resolve(".nsmp_sdk/last_sync_src/modules"),
                afterDir = projectRoot.resolve("src/main/modules")
            )
        ).flatMap(::compareDirectories)
            .sortedWith(compareBy<LocalFileChange> { it.group }.thenBy { it.relativePath })
    }

    private fun compareDirectories(pair: DirectoryPair): List<LocalFileChange> {
        val beforeFiles = collectFiles(pair.beforeDir)
        val afterFiles = collectFiles(pair.afterDir)
        val relativePaths = beforeFiles.keys + afterFiles.keys

        return relativePaths.mapNotNull { relativePath ->
            val beforePath = beforeFiles[relativePath]
            val afterPath = afterFiles[relativePath]
            val type = when {
                beforePath == null && afterPath != null -> LocalFileChangeType.ADDED
                beforePath != null && afterPath == null -> LocalFileChangeType.DELETED
                beforePath != null && afterPath != null && filesDifferent(beforePath, afterPath) -> LocalFileChangeType.MODIFIED
                else -> null
            }
            type?.let {
                LocalFileChange(
                    group = pair.name,
                    relativePath = relativePath,
                    type = it,
                    beforePath = beforePath,
                    afterPath = afterPath
                )
            }
        }
    }

    private fun collectFiles(root: Path): Map<String, Path> {
        if (!Files.isDirectory(root)) return emptyMap()
        return Files.walk(root).use { paths ->
            val result = mutableMapOf<String, Path>()
            paths
                .filter { Files.isRegularFile(it) }
                .forEach { path ->
                    result[root.relativize(path).toString().replace('\\', '/')] = path
                }
            result
        }
    }

    private fun filesDifferent(first: Path, second: Path): Boolean {
        return Files.mismatch(first, second) != -1L
    }
}

private data class DirectoryPair(
    val name: String,
    val beforeDir: Path,
    val afterDir: Path
)

data class LocalFileChange(
    val group: String,
    val relativePath: String,
    val type: LocalFileChangeType,
    val beforePath: Path?,
    val afterPath: Path?
) {
    val title: String = "$group: $relativePath"
}

enum class LocalFileChangeType {
    ADDED,
    DELETED,
    MODIFIED
}
