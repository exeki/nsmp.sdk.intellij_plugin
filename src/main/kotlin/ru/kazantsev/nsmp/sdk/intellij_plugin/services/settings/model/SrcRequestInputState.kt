package ru.kazantsev.nsmp.sdk.intellij_plugin.services.settings.model

class SrcRequestInputState(
    val allAdvImports: Boolean = false,
    val advImportsCsv: String = "",
    val advImportsExcludedCsv: String = "",

    val scriptsCsv: String = "",
    val allScripts: Boolean = false,
    val scriptsExcludedCsv: String = "",

    val allModules: Boolean = false,
    val modulesCsv: String = "",
    val modulesExcludedCsv: String = "",
)