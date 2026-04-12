package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.SrcRequestInputState
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcRequest
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.Box
import javax.swing.BoxLayout

class SrcRequestDialog(
    project: Project,
    titleText: String,
    initialState: SrcRequestInputState,
) : DialogWrapper(project) {
    private val modulesField = createTextArea(initialState.modulesCsv)
    private val allModulesField = JBCheckBox("", initialState.allModules)
    private val scriptsField = createTextArea(initialState.scriptsCsv)
    private val allScriptsField = JBCheckBox("", initialState.allScripts)
    private val advImportsField = createTextArea(initialState.advImportsCsv)
    private val allAdvImportsField = JBCheckBox("", initialState.allAdvImports)

    init {
        title = titleText
        init()
        bindAllCheckBox(allModulesField, modulesField)
        bindAllCheckBox(allScriptsField, scriptsField)
        bindAllCheckBox(allAdvImportsField, advImportsField)
    }

    override fun createCenterPanel(): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            preferredSize = Dimension(560, 460)

            add(createTextSection(MyMessageBundle.message("sync.dialog.modules"), modulesField))
            add(allModulesField.apply { text = MyMessageBundle.message("sync.dialog.all.modules") })
            add(Box.createVerticalStrut(10))

            add(createTextSection(MyMessageBundle.message("sync.dialog.scripts"), scriptsField))
            add(allScriptsField.apply { text = MyMessageBundle.message("sync.dialog.all.scripts") })
            add(Box.createVerticalStrut(10))

            add(createTextSection(MyMessageBundle.message("sync.dialog.adv.imports"), advImportsField))
            add(allAdvImportsField.apply { text = MyMessageBundle.message("sync.dialog.all.adv.imports") })
        }
    }

    override fun doValidate(): ValidationInfo? {
        val state = getInputState()
        val hasAny = state.allModules || state.allScripts || state.allAdvImports ||
            state.modulesCsv.isNotBlank() || state.scriptsCsv.isNotBlank() || state.advImportsCsv.isNotBlank()

        return if (hasAny) null else ValidationInfo(MyMessageBundle.message("sync.dialog.validation.empty.request"))
    }

    fun getInputState(): SrcRequestInputState {
        return SrcRequestInputState().also {
            it.modulesCsv = modulesField.text.trim()
            it.allModules = allModulesField.isSelected
            it.scriptsCsv = scriptsField.text.trim()
            it.allScripts = allScriptsField.isSelected
            it.advImportsCsv = advImportsField.text.trim()
            it.allAdvImports = allAdvImportsField.isSelected
        }
    }

    fun getRequest(): SrcRequest {
        val input = getInputState()
        return SrcRequest(
            modules = parseCodes(input.modulesCsv),
            allModules = input.allModules,
            scripts = parseCodes(input.scriptsCsv),
            allScripts = input.allScripts,
            advImports = parseCodes(input.advImportsCsv),
            allAdvImports = input.allAdvImports
        )
    }

    private fun parseCodes(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw
            .split(",", ";", "\n", "\r")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun createTextSection(labelText: String, field: JBTextArea): JPanel {
        return JPanel(BorderLayout(0, 6)).apply {
            alignmentX = JComponent.LEFT_ALIGNMENT
            add(JBLabel(labelText), BorderLayout.NORTH)
            add(JBScrollPane(field), BorderLayout.CENTER)
            maximumSize = Dimension(Int.MAX_VALUE, 110)
        }
    }

    private fun createTextArea(initialText: String): JBTextArea {
        return JBTextArea(initialText, 3, 60).apply {
            lineWrap = true
            wrapStyleWord = true
        }
    }

    private fun bindAllCheckBox(checkBox: JBCheckBox, textArea: JBTextArea) {
        val updateState = {
            val enabled = !checkBox.isSelected
            textArea.isEditable = enabled
            textArea.isEnabled = enabled
        }
        checkBox.addActionListener { updateState() }
        updateState()
    }
}
