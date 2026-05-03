package ru.kazantsev.nsmp.sdk.intellij_plugin.exception

import com.intellij.openapi.vfs.VirtualFile

class NotTextFileException(file: VirtualFile) : LogicException("File ${file.path} not a text file")