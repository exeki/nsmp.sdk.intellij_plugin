package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.data.SrcState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.diff_manager.model.DiffTypeFilter
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class DiffManagerTopPanel : JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)) {

    companion object {
        const val COMPONENT_HEIGHT = 28
    }

    val leftLabel = JLabel(MessageBundle.message("sync.command.diff.manager.compare.left"))
    val leftStateFilter = ComboBox(SrcState.entries.toTypedArray())
    val rightLabel = JLabel(MessageBundle.message("sync.command.diff.manager.compare.right"))
    val rightStateFilter = ComboBox(SrcState.entries.toTypedArray())
    val refreshButton = JButton("Refresh") //TODO перевести на бандл
    val updateRemoteButton = JButton(MessageBundle.message("sync.command.diff.manager.remote.update"))
    val typeLabel = JLabel(MessageBundle.message("sync.command.diff.manager.table.type"))
    val typeFilterField = ComboBox(DiffTypeFilter.entries.toTypedArray())
    val codeLabel = JLabel(MessageBundle.message("sync.command.diff.manager.table.code"))
    val codeFilterField = JBTextField(18)

    init {
        leftStateFilter.selectedItem = SrcState.CURRENT
        rightStateFilter.selectedItem = SrcState.LAST_SYNC
        add(leftLabel)
        add(leftStateFilter)
        add(rightLabel)
        add(rightStateFilter)
        add(refreshButton)
        add(updateRemoteButton)
        add(typeLabel)
        add(typeFilterField)
        add(codeLabel)
        add(codeFilterField)
    }

    override fun add(component: Component): Component? {
        val comp = super.add(component)
        if (comp is JComponent) comp.apply {
            alignmentY = CENTER_ALIGNMENT
            preferredSize = Dimension(preferredSize.width, JBUI.scale(COMPONENT_HEIGHT))
            minimumSize = Dimension(0, JBUI.scale(COMPONENT_HEIGHT))
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(COMPONENT_HEIGHT))
        }
        return comp
    }
}