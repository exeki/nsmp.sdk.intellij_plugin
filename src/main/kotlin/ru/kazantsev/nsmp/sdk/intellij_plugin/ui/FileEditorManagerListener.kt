package ru.kazantsev.nsmp.sdk.intellij_plugin.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.SyncUIAdapter
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcDtoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.dto.SrcInfoRoot
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagLayout
import java.awt.RenderingHints
import java.awt.event.ActionListener
import javax.swing.SwingConstants
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class FileEditorManagerListener(private val project: Project) : FileEditorManagerListener, DumbAware {
    private val topPanelIcon by lazy {
        runCatching {
            IconUtil.scale(
                IconLoader.getIcon("/icons/nsmp_top_panel_with_logo.svg", FileEditorManagerListener::class.java),
                null,
                1.0F
            )
        }.getOrNull()
    }

    private val syncUIAdapter: SyncUIAdapter
        get() = project.service<SyncUIAdapter>()

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (!syncUIAdapter.isSupportedFile(file)) return
        source.getEditors(file).forEach { editor ->
            installTopPanel(source, editor, file)
        }
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        if (!syncUIAdapter.isSupportedFile(file)) return
        event.manager.getEditors(file).forEach { editor ->
            installTopPanel(event.manager, editor, file)
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        source.allEditors.forEach { editor ->
            val panel = editor.getUserData(TOP_PANEL_KEY) ?: return@forEach
            val panelFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)
            if (panelFileUrl == file.url) {
                source.removeTopComponent(editor, panel)
                editor.putUserData(TOP_PANEL_KEY, null)
                editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
            }
        }
    }

    private fun installTopPanel(manager: FileEditorManager, editor: FileEditor, file: VirtualFile) {
        val existing = editor.getUserData(TOP_PANEL_KEY)
        val existingFileUrl = editor.getUserData(TOP_PANEL_FILE_URL_KEY)
        if (existing != null && existingFileUrl == file.url) return

        if (existing != null) {
            manager.removeTopComponent(editor, existing)
        }

        val actionsPanel = createActionsPanel(file)
        val actionsWrapper = JPanel(GridBagLayout()).apply {
            isOpaque = false
            add(actionsPanel)
        }

        val panel = object : JPanel(BorderLayout()) {
            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.color = JBColor(Color(74, 79, 86, 120), Color(74, 79, 86, 120))
                g2.fillRect(0, 0, width, height)
                g2.color = JBColor(Color(96, 102, 110, 130), Color(96, 102, 110, 130))
                g2.drawRect(0, 0, width - 1, height - 1)
                g2.dispose()
                super.paintComponent(g)
            }
        }.apply {
            isOpaque = false
            border = JBUI.Borders.empty(2, 4)
            topPanelIcon?.let {
                add(
                    JPanel(GridBagLayout()).apply {
                        isOpaque = false
                        add(JLabel(it).apply {
                            verticalAlignment = SwingConstants.CENTER
                            border = JBUI.Borders.emptyRight(6)
                        })
                    },
                    BorderLayout.WEST
                )
            }
            add(actionsWrapper, BorderLayout.EAST)
        }

        manager.addTopComponent(editor, panel)
        editor.putUserData(TOP_PANEL_KEY, panel)
        editor.putUserData(TOP_PANEL_FILE_URL_KEY, file.url)

        Disposer.register(editor) {
            val registeredPanel = editor.getUserData(TOP_PANEL_KEY) ?: return@register
            manager.removeTopComponent(editor, registeredPanel)
            editor.putUserData(TOP_PANEL_KEY, null)
            editor.putUserData(TOP_PANEL_FILE_URL_KEY, null)
        }
    }

    private fun createActionsPanel(file: VirtualFile): JPanel {
        return JPanel(FlowLayout(FlowLayout.RIGHT, 4, 0)).apply {
            isOpaque = false
            border = JBUI.Borders.empty()
            add(
                createActionButton("Pull") {
                    syncUIAdapter.pull(
                        file = file,
                        onSuccessCallback = { value: SrcDtoRoot ->
                            //TODO нотификация о том, что все хорошо
                        },
                        onFailureCallback = { e: Throwable ->
                            //TODO popup с информацией о ошибке
                        }
                    )
                }
            )
            add(
                createActionButton("SyncCheck") {
                    syncUIAdapter.syncCheck(
                        file = file,
                        onSuccessCallback = { value: SrcInfoRoot ->
                            //TODO нотификация о том, что все хорошо
                        },
                        onFailureCallback = { e: Throwable ->
                            //TODO popup с информацией о ошибке
                        }
                    )
                }
            )
            add(
                createActionButton("Push") {
                    syncUIAdapter.push(
                        file = file,
                        force = false,
                        onSuccessCallback = { value: SrcInfoRoot ->
                            //TODO нотификация о том, что все хорошо
                            //если e это SyncCheckFailedException, то отдельная обработка
                        },
                        onFailureCallback = { e: Throwable ->
                            //TODO popup с информацией о ошибке
                        }
                    )
                }
            )
        }
    }

    private fun createActionButton(title: String, actionListener: ActionListener): JButton {
        return object : JButton(title) {
            override fun getPreferredSize(): Dimension {
                val fm = getFontMetrics(font)
                val horizontalPadding = 10
                val width = fm.stringWidth(text ?: "") + horizontalPadding
                return Dimension(width, 18)
            }

            override fun getMinimumSize(): Dimension = preferredSize

            override fun getMaximumSize(): Dimension = preferredSize

            override fun updateUI() {
                super.updateUI()
                isRolloverEnabled = true
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val fillColor = when {
                    model.isPressed -> JBColor(Color(140, 146, 156, 190), Color(120, 126, 136, 190))
                    model.isRollover -> JBColor(Color(130, 136, 146, 165), Color(110, 116, 126, 165))
                    else -> JBColor(Color(120, 126, 136, 140), Color(96, 102, 112, 140))
                }
                g2.color = fillColor
                g2.fillRoundRect(0, 0, width - 1, height - 1, 8, 8)
                g2.dispose()
                super.paintComponent(g)
            }

            override fun paintBorder(g: Graphics) {
                // Flat button without inner contour.
            }
        }.apply {
            isFocusable = false
            isOpaque = false
            isContentAreaFilled = false
            isBorderPainted = false
            foreground = JBColor(0xD8DCE2, 0xD8DCE2)
            font = font.deriveFont(font.size2D - 1f)
            margin = JBUI.emptyInsets()
            addActionListener(actionListener)
        }
    }

    private companion object {
        private val TOP_PANEL_KEY = Key.create<JComponent>("nsmp.sdk.groovy.sync.top.panel")
        private val TOP_PANEL_FILE_URL_KEY = Key.create<String>("nsmp.sdk.groovy.sync.top.panel.file.url")
    }
}
