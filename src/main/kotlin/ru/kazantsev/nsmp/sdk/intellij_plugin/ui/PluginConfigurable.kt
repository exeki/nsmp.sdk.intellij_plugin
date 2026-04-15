package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import ru.kazantsev.nsmp.basic_api_connector.ConfigService
import ru.kazantsev.nsmp.basic_api_connector.dto.InstallationDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.MyMessageBundle
import ru.kazantsev.nsmp.sdk.intellij_plugin.server.service.NsdHttpServerService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.AppSettingsService
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.ProjectSettingsService
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.BoxLayout
import javax.swing.table.DefaultTableModel

class PluginConfigurable(private val project: Project) : Configurable {
    private var panel: JPanel? = null
    private var portField: JBTextField? = null
    private var configPathField: JBTextField? = null
    private var installationsTable: JBTable? = null
    private var installationsTableModel: DefaultTableModel? = null
    private var installationBox: ComboBox<InstallationOption>? = null
    private var installations: MutableList<InstallationDto> = mutableListOf()

    private val projectSettings: ProjectSettingsService
        get() = ProjectSettingsService.getInstance(project)
    private val configService: ConfigService
        get() = ConfigService(AppSettingsService.getInstance().connectorConfigPath)

    override fun getDisplayName(): String = MyMessageBundle.message("settings.display.name")

    override fun createComponent(): JComponent {
        if (panel == null) {
            val port = JBTextField()
            val configPath = JBTextField()
            val model = createTableModel()
            val table = JBTable(model)
            val projectInstallationBox = ComboBox<InstallationOption>().apply {
                renderer = SimpleListCellRenderer.create("") { value ->
                    value.label
                }
            }

            val controls = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
                add(JButton(MyMessageBundle.message("settings.global.button.add")).apply { addActionListener { addInstallation() } })
                add(JButton(MyMessageBundle.message("settings.global.button.edit")).apply { addActionListener { editInstallation() } })
                add(JButton(MyMessageBundle.message("settings.global.button.delete")).apply { addActionListener { deleteInstallation() } })
                add(JButton(MyMessageBundle.message("settings.global.button.reload")).apply { addActionListener { reloadInstallationsFromPath() } })
            }

            val tablePanel = JPanel(BorderLayout()).apply {
                add(JBScrollPane(table), BorderLayout.CENTER)
                add(controls, BorderLayout.SOUTH)
            }

            val globalSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(MyMessageBundle.message("settings.global.server.port"), port)
                .addLabeledComponent(MyMessageBundle.message("settings.global.connector.config.path"), configPath)
                .addComponentFillVertically(tablePanel, 0)
                .panel
                .apply { border = BorderFactory.createTitledBorder(MyMessageBundle.message("settings.global.title")) }

            val projectSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(MyMessageBundle.message("settings.project.installation"), projectInstallationBox)
                .addComponentFillVertically(JPanel(), 0)
                .panel
                .apply { border = BorderFactory.createTitledBorder(MyMessageBundle.message("settings.project.title")) }

            panel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createEmptyBorder(8, 12, 8, 12)
                add(globalSettingsPanel)
                add(JLabel(" ").apply { horizontalAlignment = SwingConstants.LEFT })
                add(projectSettingsPanel)
            }

            portField = port
            configPathField = configPath
            installationsTable = table
            installationsTableModel = model
            installationBox = projectInstallationBox
        }

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsService.getInstance()
        if (portField?.text?.trim() != settings.serverPort.toString()) return true
        if (configPathField?.text?.trim() != settings.connectorConfigPath) return true
        if (installations != configService.installations) return true
        val selectedId = (installationBox?.selectedItem as? InstallationOption)?.id.orEmpty()
        return selectedId != projectSettings.selectedInstallationId
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = AppSettingsService.getInstance()
        val port = parsePort(portField?.text)
        val configPath = configPathField?.text?.trim().orEmpty()
        if (configPath.isBlank()) throw ConfigurationException(MyMessageBundle.message("settings.error.config.path.required"))

        runCatching { configService.saveInstallations(installations) }
            .getOrElse {
                throw ConfigurationException(
                    MyMessageBundle.message("settings.error.installations.save.failed", it.message ?: "")
                )
            }

        settings.serverPort = port
        settings.connectorConfigPath = configPath
        projectSettings.selectedInstallationId = (installationBox?.selectedItem as? InstallationOption)?.id.orEmpty()
        NsdHttpServerServiceHolder.restartServer()
    }

    override fun reset() {
        val settings = AppSettingsService.getInstance()
        portField?.text = settings.serverPort.toString()
        configPathField?.text = settings.connectorConfigPath
        installations = configService.getInstallations().toMutableList()
        refreshTable()
        refreshProjectInstallations(projectSettings.selectedInstallationId)
    }

    override fun disposeUIResources() {
        panel = null
        portField = null
        configPathField = null
        installationsTable = null
        installationsTableModel = null
        installationBox = null
        installations = mutableListOf()
    }

    private fun addInstallation() {
        val selectedProjectInstallationId = (installationBox?.selectedItem as? InstallationOption)?.id
        val dialog = InstallationDialog(
            project = project,
            titleText = MyMessageBundle.message("installation.dialog.title.add"),
            initialValue = null
        )
        if (!dialog.showAndGet()) return
        val installation = dialog.getInstallation()
        val existingIndex = installations.indexOfFirst { it.id == installation.id }
        if (existingIndex >= 0) installations[existingIndex] = installation else installations.add(installation)
        refreshTable()
        refreshProjectInstallations(selectedProjectInstallationId)
    }

    private fun editInstallation() {
        val selected = selectedInstallationIndex() ?: return
        val dialog = InstallationDialog(
            project = project,
            titleText = MyMessageBundle.message("installation.dialog.title.edit"),
            initialValue = installations[selected]
        )
        if (!dialog.showAndGet()) return
        val updated = dialog.getInstallation()
        installations[selected] = updated
        refreshTable()
        refreshProjectInstallations(updated.id)
        installationsTable?.setRowSelectionInterval(selected, selected)
    }

    private fun deleteInstallation() {
        val selected = selectedInstallationIndex() ?: return
        val selectedProjectInstallationId = (installationBox?.selectedItem as? InstallationOption)?.id
        val removedId = installations[selected].id
        installations.removeAt(selected)
        refreshTable()
        refreshProjectInstallations(
            preferredId = selectedProjectInstallationId.takeIf { it != removedId }
        )
    }

    private fun reloadInstallationsFromPath() {
        val path = configPathField?.text?.trim().orEmpty()
        if (path.isBlank()) {
            Messages.showErrorDialog(
                MyMessageBundle.message("settings.error.config.path.required"),
                MyMessageBundle.message("settings.display.name")
            )
            return
        }
        installations = configService.getInstallations().toMutableList()
        refreshTable()
        refreshProjectInstallations((installationBox?.selectedItem as? InstallationOption)?.id)
    }

    private fun selectedInstallationIndex(): Int? {
        val selectedRow = installationsTable?.selectedRow ?: -1
        if (selectedRow < 0 || selectedRow >= installations.size) {
            Messages.showInfoMessage(
                MyMessageBundle.message("settings.info.select.installation"),
                MyMessageBundle.message("settings.display.name")
            )
            return null
        }
        return selectedRow
    }

    private fun refreshProjectInstallations(preferredId: String?) {
        val box = installationBox ?: return
        box.removeAllItems()
        box.addItem(InstallationOption.None)
        installations.forEach { box.addItem(InstallationOption(id = it.id, label = it.id)) }

        val idToSelect = preferredId?.takeIf { it.isNotBlank() }
        if (idToSelect != null) {
            val selectedOption = (0 until box.itemCount)
                .mapNotNull { box.getItemAt(it) }
                .firstOrNull { it.id == idToSelect }
            box.selectedItem = selectedOption ?: InstallationOption.None
        } else {
            box.selectedItem = InstallationOption.None
        }
    }

    private fun refreshTable() {
        val model = installationsTableModel ?: return
        model.rowCount = 0
        installations.forEach { installation ->
            model.addRow(
                arrayOf<Any>(
                    installation.id,
                    installation.scheme,
                    installation.host,
                    maskSecret(installation.accessKey),
                    installation.ignoreSSL,
                )
            )
        }
    }

    private fun createTableModel(): DefaultTableModel {
        return object : DefaultTableModel(
            arrayOf(
                MyMessageBundle.message("settings.table.id"),
                MyMessageBundle.message("settings.table.scheme"),
                MyMessageBundle.message("settings.table.host"),
                MyMessageBundle.message("settings.table.access.key"),
                MyMessageBundle.message("settings.table.ignore.ssl"),
            ),
            0
        ) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false
        }
    }

    private fun parsePort(value: String?): Int {
        val port = value?.trim()?.toIntOrNull()
            ?: throw ConfigurationException(MyMessageBundle.message("settings.error.server.port.integer"))
        if (port !in 1..65535) throw ConfigurationException(MyMessageBundle.message("settings.error.server.port.range"))
        return port
    }

    private fun maskSecret(secret: String): String = if (secret.isBlank()) "" else "********"
}

private data class InstallationOption(
    val id: String,
    val label: String,
) {
    companion object {
        val None: InstallationOption = InstallationOption(
            id = "",
            label = MyMessageBundle.message("settings.project.installation.not.selected")
        )
    }
}

private object NsdHttpServerServiceHolder {
    fun restartServer() {
        val service = ApplicationManager.getApplication().getService(NsdHttpServerService::class.java)
        runCatching { service.restart() }
            .onFailure {
                Messages.showErrorDialog(
                    MyMessageBundle.message("settings.error.server.restart.failed", it.message ?: ""),
                    MyMessageBundle.message("settings.display.name")
                )
            }
    }
}
