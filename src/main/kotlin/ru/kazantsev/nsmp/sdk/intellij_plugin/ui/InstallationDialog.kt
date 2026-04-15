package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import ru.kazantsev.nsmp.basic_api_connector.dto.InstallationDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import javax.swing.JComponent

class InstallationDialog(
    project: Project?,
    private val titleText: String,
    initialValue: InstallationDto?,
) : DialogWrapper(project) {
    private val idField = JBTextField(initialValue?.id.orEmpty())
    private val schemeField = ComboBox(arrayOf("https", "http")).apply {
        selectedItem = if (initialValue?.scheme == "http") "http" else "https"
    }
    private val hostField = JBTextField(initialValue?.host.orEmpty())
    private val accessKeyField = JBPasswordField().apply { text = initialValue?.accessKey.orEmpty() }
    private val ignoreSslField = JBCheckBox("", initialValue?.ignoreSSL ?: true)

    init {
        title = titleText
        init()
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(MyMessageBundle.message("installation.dialog.id"), idField)
            .addLabeledComponent(MyMessageBundle.message("installation.dialog.scheme"), schemeField)
            .addLabeledComponent(MyMessageBundle.message("installation.dialog.host"), hostField)
            .addLabeledComponent(MyMessageBundle.message("installation.dialog.access.key"), accessKeyField)
            .addLabeledComponent(MyMessageBundle.message("installation.dialog.ignore.ssl"), ignoreSslField)
            .panel
    }

    override fun doValidate(): ValidationInfo? {
        if (idField.text.trim().isEmpty()) return ValidationInfo(MyMessageBundle.message("installation.dialog.validation.id.required"), idField)
        if (hostField.text.trim().isEmpty()) return ValidationInfo(MyMessageBundle.message("installation.dialog.validation.host.required"), hostField)
        if (accessKeyField.text.trim().isEmpty()) return ValidationInfo(
            MyMessageBundle.message("installation.dialog.validation.access.key.required"),
            accessKeyField
        )
        return null
    }

    fun getInstallation(): InstallationDto {
        val accessKey = String(accessKeyField.password).trim()
        return InstallationDto(
            idField.text.trim(),
            schemeField.selectedItem?.toString() ?: "https",
            hostField.text.trim(),
            accessKey,
            ignoreSslField.isSelected,
        )
    }
}
