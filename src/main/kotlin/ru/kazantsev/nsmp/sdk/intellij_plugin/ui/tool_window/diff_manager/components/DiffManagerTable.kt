package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.components

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.RemoteSrcStateService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.state.DiffService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.DiffStatus
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffRow
import ru.kazantsev.nsmp.sdk.sources_sync.data.request.SrcRequest
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Files
import java.util.regex.Pattern
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.RowFilter
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.TableRowSorter
import kotlin.String

class DiffManagerTable(private val project: Project) : JPanel(BorderLayout()) {
    private var updatingCompareSetControls = false
    private var refreshGeneration = 0L
    private val diffService = project.service<DiffService>()
    private val remoteSrcStateService = project.service<RemoteSrcStateService>()
    private val topPanel = DiffManagerTopPanel()
    private val model = DiffManagerTableModel()
    private val sorter = TableRowSorter(model)
    private val table = JBTable(model).apply {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        rowSorter = sorter
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (selectedRow < 0) return
                openDiffForSelectedRowAsync()
            }
        })
    }
    private val loadingPanel = JBLoadingPanel(BorderLayout(), project).apply {
        add(JBScrollPane(table), BorderLayout.CENTER)
    }

    init {
        border = JBUI.Borders.empty()
        topPanel.apply {
            leftStateFilter.addActionListener { refresh() }
            rightStateFilter.addActionListener { refresh() }
            refreshButton.addActionListener { refresh() }
            updateRemoteButton.addActionListener { onUpdateRemoteClicked() }
            typeFilterField.addActionListener { applyFilters() }
            codeFilterField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = applyFilters()
                override fun removeUpdate(e: DocumentEvent?) = applyFilters()
                override fun changedUpdate(e: DocumentEvent?) = applyFilters()
            })
        }
        add(topPanel, BorderLayout.NORTH)
        add(loadingPanel, BorderLayout.CENTER)
        refresh()
    }

    fun refresh() {
        if (updatingCompareSetControls) return
        updatingCompareSetControls = true
        val left = (topPanel.leftStateFilter.selectedItem as? SrcState) ?: SrcState.CURRENT
        val right = (topPanel.rightStateFilter.selectedItem as? SrcState) ?: SrcState.LAST_SYNC
        val req = SrcRequest(allScripts = true, allModules = true, allAdvImports = true)
        val currentGeneration = ++refreshGeneration
        loadingPanel.startLoading()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Loading diff rows", true) {
            private var rows: List<DiffRow> = emptyList()

            override fun run(indicator: ProgressIndicator) {
                val diffRows = mutableListOf<DiffRow>()
                diffService.getDiffPairs(req, left, right)
                    .forEach { (type, pairs) ->
                    pairs
                        .filter { it.diffStatus != DiffStatus.UNCHANGED }
                        .forEach { pair ->
                        diffRows.add(
                            DiffRow(
                                type = type,
                                code = pair.code,
                                leftSet = left,
                                leftFile = pair.left?.virtualFile,
                                rightSet = right,
                                rightFile = pair.right?.virtualFile,
                                status = pair.diffStatus
                            )
                        )
                    }
                }
                rows = diffRows
            }

            override fun onSuccess() {
                if (currentGeneration != refreshGeneration) return
                setRows(rows)
            }

            override fun onFinished() {
                if (currentGeneration == refreshGeneration) {
                    updatingCompareSetControls = false
                    loadingPanel.stopLoading()
                }
            }
        })
    }

    fun onUpdateRemoteClicked() {
        val backgroundTaskTitle = "NSMP SDK Update remote src state"
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, backgroundTaskTitle, true) {
            private var shouldRefresh = false

            override fun run(indicator: ProgressIndicator) {
                val req = SrcRequest(allScripts = true, allAdvImports = true, allModules = true)
                remoteSrcStateService.updateRemoteStateBackground(req)
                val items = listOf(topPanel.leftStateFilter.selectedItem, topPanel.rightStateFilter.selectedItem)
                shouldRefresh = items.contains(SrcState.REMOTE)
            }

            override fun onSuccess() {
                if (shouldRefresh) refresh()
            }
        })
    }

    fun setRows(rows: List<DiffRow>) {
        model.setRows(rows)
        applyFilters()
    }


    override fun add(component: Component): Component? {
        if (component is JComponent) component.alignmentY = CENTER_ALIGNMENT
        return super.add(component)
    }

    fun applyFilters() {
        val codeText = topPanel.codeFilterField.text.trim()
        val selectedType =
            topPanel.typeFilterField.selectedItem as? ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter

        val filters = mutableListOf<RowFilter<DiffManagerTableModel, Int>>()
        if (selectedType != null && selectedType != ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter.ALL) {
            filters += RowFilter.regexFilter("^${Pattern.quote(selectedType.code)}$", 0)
        }
        if (codeText.isNotBlank()) {
            filters += RowFilter.regexFilter("(?i)${Pattern.quote(codeText)}", 1)
        }

        sorter.rowFilter = if (filters.isEmpty()) null else RowFilter.andFilter(filters)
    }

    private fun openDiffForSelectedRowAsync() {
        val selected = table.selectedRow
        if (selected < 0) return
        val modelRow = table.convertRowIndexToModel(selected)
        val row = model.rowAt(modelRow) ?: return
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Loading diff content", true) {
            private var leftText = ""
            private var rightText = ""
            private var error: Throwable? = null

            override fun run(indicator: ProgressIndicator) {
                try {
                    leftText = readText(row.leftFile)
                    rightText = readText(row.rightFile)
                } catch (t: Throwable) {
                    error = t
                }
            }

            override fun onSuccess() {
                val failure = error
                if (failure != null) {
                    Messages.showErrorDialog(
                        project,
                        failure.message ?: "Failed to load compared files.",
                        "Diff Manager"
                    )
                    return
                }
                val request = SimpleDiffRequest(
                    "${row.type.code}: ${row.code}",
                    DiffContentFactory.getInstance().create(leftText),
                    DiffContentFactory.getInstance().create(rightText),
                    row.leftSet.code,
                    row.rightSet.code
                )
                DiffManager.getInstance().showDiff(project, request)
            }
        })
    }

    private fun readText(file: VirtualFile?): String = file
        ?.takeIf(VirtualFile::exists)
        ?.let(VirtualFile::toNioPath)
        ?.let(Files::readString)
        .orEmpty()
}
