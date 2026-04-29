package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.CompareSetType
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DiffManagerTopPanel : JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)) {

    private val leftLabel = JLabel(MessageBundle.message("sync.command.local.changes.compare.left"))
    private val leftSetFilter = ComboBox(CompareSetType.entries.toTypedArray()).apply {
        addActionListener { handleCompareSetChange() }
        alignmentY = CENTER_ALIGNMENT
        renderer = DefaultListCellRenderer().apply {
            horizontalAlignment = JLabel.LEFT
        }
    }
    private val rightLabel = JLabel(MessageBundle.message("sync.command.local.changes.compare.right"))
    private val rightSetFilter = ComboBox(CompareSetType.entries.toTypedArray()).apply {
        addActionListener { handleCompareSetChange() }
    }
    private val updateRemoteButton = JButton(
        MessageBundle.message("sync.command.local.changes.remote.update")
    ).apply {
        addActionListener { onUpdateRemoteClicked() }
    }
    private val typeLabel = JLabel(MessageBundle.message("sync.command.local.changes.table.type"))
    private val typeFilterField = ComboBox(arrayOf("ALL", "SCRIPT", "MODULE", "ADV_IMPORT")).apply {
        addActionListener { applyFilters() }
    }
    private val sourceCodeLabel = JLabel(MessageBundle.message("sync.command.local.changes.table.code"))
    private val sourceCodeFilterField = JBTextField(18).apply {
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = applyFilters()
            override fun removeUpdate(e: DocumentEvent?) = applyFilters()
            override fun changedUpdate(e: DocumentEvent?) = applyFilters()
        })
    }

    init {
        add(leftLabel)
        add(leftSetFilter)
        add(rightLabel)
        add(rightSetFilter)
        add(updateRemoteButton)
        add(typeLabel)
        add(typeFilterField)
        add(sourceCodeLabel)
        add(sourceCodeFilterField)
    }

    override fun add(component: Component): Component {
        if (component is JComponent) {
            component.alignmentY = CENTER_ALIGNMENT
        }
        return component
    }
}