package ru.kazantsev.nsmp.sdk.intellij_plugin.exception

import com.intellij.openapi.vfs.VirtualFile

class FileExecuteExtensionException(file : VirtualFile) : LogicException("File ${file.path} is not a Groovy File")