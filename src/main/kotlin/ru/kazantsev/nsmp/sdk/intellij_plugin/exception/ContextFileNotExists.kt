package ru.kazantsev.nsmp.sdk.intellij_plugin.exception

class ContextFileNotExists(contextFileRelativePath: String) :
    LogicException("Context file $contextFileRelativePath not exists")