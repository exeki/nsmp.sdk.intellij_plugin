package ru.kazantsev.nsmp.sdk.intellij_plugin.exception

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class UnselectedProjectInstallation : LogicException(MessageBundle.message("sync.error.installation.not.selected"))