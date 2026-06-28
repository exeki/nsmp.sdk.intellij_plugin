package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import ru.kazantsev.nsmp.sdk.sources_sync.Constants

/**
 * Константы генерации Java-заглушек метаклассов и формата комментария привязки.
 */
object MetaClassGenerationSettings {
    private const val GENERATED_METACLASSES_DIR = "generated/metaclasses"

    val GENERATED_SOURCES_RELATIVE_PATH: String =
        "${Constants.SDK_DIR}/$GENERATED_METACLASSES_DIR"

    const val PACKAGE_NAME = "ru.naumen.core.server.script.spi"
    const val CLASS_NAME_POSTFIX = "SDO"
    const val CLASS_DELIMITER = '_'
    const val META_COMMENT_PREFIX = "nsmp:meta"
}
