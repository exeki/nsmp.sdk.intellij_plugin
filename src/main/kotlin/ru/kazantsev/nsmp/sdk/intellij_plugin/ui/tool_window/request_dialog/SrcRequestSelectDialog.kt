package ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.notification.DialogNotificationService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.model.SrcRequestSelectState
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.options_provider.SrcOptionsProvider
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.components.CollapsibleSection
import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.tool_window.request_dialog.components.MultiSelectField
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

/**
 * Диалог выбора всех трех видов исходников пред отправкой запроса
 */
open class SrcRequestSelectDialog(
    title: String,
    private val project: Project,
    private val withForceCheckbox: Boolean,
    private val optionsProvider: SrcOptionsProvider,
    private val action: (SrcRequestSelectState) -> Unit
) : DialogWrapper(project) {

    private val projectSettings: ProjectSettingsService
        get() = project.service<ProjectSettingsService>()
    private val dialogNotificationService: DialogNotificationService
        get() = project.service<DialogNotificationService>()

    private val initialState: SrcRequestSelectState = projectSettings.state.savedMultiSelectRequestInput

    private val modulesField = MultiSelectField(
        project = project,
        chooserTitle = MessageBundle.message("sync.dialog.modules.selector"),
        initialValues = initialState.modules,
        optionsProvider = optionsProvider::loadModulesOptions,
        onLoadError = ::showOptionsLoadError,
    )
    private val allModulesField = JBCheckBox("", initialState.allModules)
    private val modulesExcludedField = MultiSelectField(
        project = project,
        chooserTitle = MessageBundle.message("sync.dialog.modules.excluded.selector"),
        initialValues = initialState.modulesExcluded,
        optionsProvider = optionsProvider::loadModulesOptions,
        onLoadError = ::showOptionsLoadError,
    )
    private val modulesSection = CollapsibleSection(
        title = MessageBundle.message("sync.dialog.modules.selector"),
        allCheckBox = allModulesField,
        includeField = modulesField,
        excludedField = modulesExcludedField,
        allText = MessageBundle.message("sync.dialog.all.modules")
    )

    private val scriptsField = MultiSelectField(
        project = project,
        chooserTitle = MessageBundle.message("sync.dialog.scripts.selector"),
        initialValues = initialState.scripts,
        optionsProvider = optionsProvider::loadScriptsOptions,
        onLoadError = ::showOptionsLoadError,
    )
    private val allScriptsField = JBCheckBox("", initialState.allScripts)
    private val scriptsExcludedField = MultiSelectField(
        project = project,
        chooserTitle = MessageBundle.message("sync.dialog.scripts.excluded.selector"),
        initialValues = initialState.scriptsExcluded,
        optionsProvider = optionsProvider::loadScriptsOptions,
        onLoadError = ::showOptionsLoadError,
    )
    private val scriptsSection = CollapsibleSection(
        title = MessageBundle.message("sync.dialog.scripts.selector"),
        allCheckBox = allScriptsField,
        includeField = scriptsField,
        excludedField = scriptsExcludedField,
        allText = MessageBundle.message("sync.dialog.all.scripts")
    )

    private val advImportsField = MultiSelectField(
        project = project,
        chooserTitle = MessageBundle.message("sync.dialog.adv.imports.selector"),
        initialValues = initialState.advImports,
        optionsProvider = optionsProvider::loadAdvImportsOptions,
        onLoadError = ::showOptionsLoadError,
    )
    private val allAdvImportsField = JBCheckBox("", initialState.allAdvImports)
    private val advImportsExcludedField = MultiSelectField(
        project = project,
        chooserTitle = MessageBundle.message("sync.dialog.adv.imports.excluded.selector"),
        initialValues = initialState.advImportsExcluded,
        optionsProvider = optionsProvider::loadAdvImportsOptions,
        onLoadError = ::showOptionsLoadError,
    )
    private val advImportsSection = CollapsibleSection(
        title = MessageBundle.message("sync.dialog.adv.imports.selector"),
        allCheckBox = allAdvImportsField,
        includeField = advImportsField,
        excludedField = advImportsExcludedField,
        allText = MessageBundle.message("sync.dialog.all.adv.imports")
    )

    private val forceWarningLabel = JBLabel(MessageBundle.message("sync.dialog.force.warning"))
    private val forceField = JBCheckBox(MessageBundle.message("sync.dialog.force"), initialState.force)
    private val clearAllButton = JButton(MessageBundle.message("sync.dialog.clear.all"))

    init {
        this.title = title
        preloadOptions()
        bindAllCheckBox(allModulesField, modulesField)
        bindAllCheckBox(allScriptsField, scriptsField)
        bindAllCheckBox(allAdvImportsField, advImportsField)
        allModulesField.text = MessageBundle.message("sync.dialog.all.modules")
        allScriptsField.text = MessageBundle.message("sync.dialog.all.scripts")
        allAdvImportsField.text = MessageBundle.message("sync.dialog.all.adv.imports")
        clearAllButton.addActionListener { clearAllValues() }
        init()
    }

    private fun preloadOptions() {
        optionsProvider.loadModulesOptions().whenComplete { _, error ->
            if (error != null && !project.isDisposed) {
                showOptionsLoadError(error.cause ?: error)
            }
        }
        optionsProvider.loadScriptsOptions().whenComplete { _, error ->
            if (error != null && !project.isDisposed) {
                showOptionsLoadError(error.cause ?: error)
            }
        }
        optionsProvider.loadAdvImportsOptions().whenComplete { _, error ->
            if (error != null && !project.isDisposed) {
                showOptionsLoadError(error.cause ?: error)
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = JComponent.LEFT_ALIGNMENT

            add(modulesSection.component)
            add(Box.createVerticalStrut(10))
            add(scriptsSection.component)
            add(Box.createVerticalStrut(10))
            add(advImportsSection.component)

            if (withForceCheckbox) {
                add(Box.createVerticalStrut(10))
                add(
                    JPanel(BorderLayout(8, 0)).apply {
                        alignmentX = JComponent.LEFT_ALIGNMENT
                        add(forceField.apply {
                            alignmentX = JComponent.LEFT_ALIGNMENT
                        }, BorderLayout.WEST)
                        add(forceWarningLabel.apply {
                            alignmentX = JComponent.LEFT_ALIGNMENT
                        }, BorderLayout.CENTER)
                        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
                    }
                )
            }

            add(Box.createVerticalStrut(10))
            add(clearAllButton.apply {
                alignmentX = JComponent.LEFT_ALIGNMENT
            })
        }
        return JBScrollPane(contentPanel).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            preferredSize = Dimension(700, 560)
            border = JBUI.Borders.empty()
        }
    }

    override fun doValidate(): ValidationInfo? {
        val state = getInputState()
        val hasAny = state.allModules || state.allScripts || state.allAdvImports ||
                state.modules.isNotEmpty() || state.scripts.isNotEmpty() || state.advImports.isNotEmpty()

        return if (hasAny) null else ValidationInfo(MessageBundle.message("sync.dialog.validation.empty.request"))
    }

    override fun doOKAction() {
        projectSettings.state.savedMultiSelectRequestInput = getInputState()
        action(getInputState())
        super.doOKAction()
    }

    fun getInputState(): SrcRequestSelectState {
        return SrcRequestSelectState().apply {
            modules = modulesField.getValues().toMutableList()
            allModules = allModulesField.isSelected
            modulesExcluded = modulesExcludedField.getValues().toMutableList()
            scripts = scriptsField.getValues().toMutableList()
            allScripts = allScriptsField.isSelected
            scriptsExcluded = scriptsExcludedField.getValues().toMutableList()
            advImports = advImportsField.getValues().toMutableList()
            advImportsExcluded = advImportsExcludedField.getValues().toMutableList()
            allAdvImports = allAdvImportsField.isSelected
            force = forceField.isSelected
        }
    }

    private fun showOptionsLoadError(error: Throwable) {
        dialogNotificationService.showError(
            title = MessageBundle.message("sync.dialog.options.load.error.title"),
            error = error
        )
    }

    private fun clearAllValues() {
        modulesSection.clearSectionValues()
        scriptsSection.clearSectionValues()
        advImportsSection.clearSectionValues()
        forceField.isSelected = false
    }

    private fun bindAllCheckBox(checkBox: JBCheckBox, field: MultiSelectField) {
        val updateState = {
            field.setSelectionEnabled(!checkBox.isSelected)
            field.notifyChanged()
        }
        checkBox.addActionListener { updateState() }
        updateState()
    }

}
