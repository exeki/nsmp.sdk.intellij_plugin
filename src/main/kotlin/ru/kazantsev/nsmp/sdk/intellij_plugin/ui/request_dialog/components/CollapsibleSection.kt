package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.request_dialog.components

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.MessageBundle
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

class CollapsibleSection(
    private val title: String,
    private val allCheckBox: JBCheckBox,
    private val includeField: MultiSelectField,
    private val excludedField: MultiSelectField,
    private val allText: String,
) {
    private var expanded = false
    private val toggleButton = JButton()
    private val summaryLabel = JBLabel()
    private val clearButton = JButton(MessageBundle.message("sync.dialog.clear.section"))
    private val headerPanel = createHeader()
    private val contentPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        alignmentX = JComponent.LEFT_ALIGNMENT
        border = JBUI.Borders.empty(10, 12, 12, 12)
        add(allCheckBox.apply { alignmentX = JComponent.LEFT_ALIGNMENT })
        add(Box.createVerticalStrut(8))
        add(includeField.createSectionComponent(MessageBundle.message("sync.dialog.section.selected")))
        add(Box.createVerticalStrut(8))
        add(excludedField.createSectionComponent(MessageBundle.message("sync.dialog.section.excluded")))
    }

    val component: JComponent = JPanel(BorderLayout()).apply {
        alignmentX = JComponent.LEFT_ALIGNMENT
        border = CompoundBorder(
            LineBorder(JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground(), 1),
            JBUI.Borders.empty()
        )
        add(headerPanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    init {
        includeField.addChangeListener { refreshHeader() }
        excludedField.addChangeListener { refreshHeader() }
        allCheckBox.addActionListener { refreshHeader() }
        clearButton.addActionListener { clearSectionValues() }
        refreshHeader()
        updateExpandedState()
    }

    private fun createHeader(): JComponent {
        return JPanel(BorderLayout(8, 0)).apply {
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = JBUI.Borders.empty(4, 10)
            add(toggleButton.apply {
                horizontalAlignment = SwingConstants.LEFT
                isBorderPainted = false
                isContentAreaFilled = false
                alignmentX = JComponent.LEFT_ALIGNMENT
                addActionListener {
                    expanded = !expanded
                    updateExpandedState()
                }
            }, BorderLayout.WEST)
            add(
                JPanel(BorderLayout(8, 0)).apply {
                    isOpaque = false
                    add(summaryLabel, BorderLayout.CENTER)
                    add(clearButton, BorderLayout.EAST)
                },
                BorderLayout.EAST
            )
        }
    }

    private fun refreshHeader() {
        toggleButton.text = "${if (expanded) "-" else "+"} $title"
        summaryLabel.text = buildSummary()
    }

    private fun updateExpandedState() {
        contentPanel.isVisible = expanded
        refreshHeader()
        val headerHeight = headerPanel.preferredSize.height
        val contentHeight = contentPanel.preferredSize.height
        val insets = component.border?.getBorderInsets(component)
        val borderInsets = (insets?.top ?: 0) + (insets?.bottom ?: 0)
        val targetHeight = if (expanded) headerHeight + contentHeight + borderInsets
        else headerHeight + borderInsets
        component.preferredSize = Dimension(component.preferredSize.width.coerceAtLeast(1), targetHeight)
        component.maximumSize = Dimension(Int.MAX_VALUE, targetHeight)
        component.minimumSize = Dimension(0, targetHeight)
        component.revalidate()
        component.repaint()
    }

    fun clearSectionValues() {
        includeField.clear()
        excludedField.clear()
        allCheckBox.isSelected = false
        includeField.setSelectionEnabled(true)
        includeField.notifyChanged()
        excludedField.notifyChanged()
        updateExpandedState()
    }

    private fun buildSummary(): String {
        if (allCheckBox.isSelected) {
            val excludedCount = excludedField.getValues().size
            return if (excludedCount == 0) allText
            else MessageBundle.message("sync.dialog.section.summary.all.with.excluded", excludedCount)
        }
        val selectedCount = includeField.getValues().size
        val excludedCount = excludedField.getValues().size
        return if (selectedCount == 0 && excludedCount == 0) MessageBundle.message("sync.dialog.multi.select.none")
        else MessageBundle.message("sync.dialog.section.summary.selected.excluded", selectedCount, excludedCount)
    }
}
