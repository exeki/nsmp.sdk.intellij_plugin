package ru.kazantsev.nsmp.sdk.intellij_plugin.exception

import ru.kazantsev.nsmp.sdk.intellij_plugin.ui.MessageBundle

class SrcSyncUnsupportedType : LogicException(MessageBundle.message("sync.error.file.type.unknown"))