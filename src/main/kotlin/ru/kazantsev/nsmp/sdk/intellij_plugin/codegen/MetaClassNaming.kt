package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

/**
 * Преобразует код метакласса NSMP в имя и FQN сгенерированного Java-класса.
 */
object MetaClassNaming {

    fun classNameFromMetaCode(metaCode: String): String {
        val parts = metaCode.split("$").map { part ->
            part.replaceFirstChar { char -> char.uppercaseChar() }
        }
        return (parts + MetaClassGenerationSettings.CLASS_NAME_POSTFIX).joinToString(
            MetaClassGenerationSettings.CLASS_DELIMITER.toString()
        )
    }

    fun qualifiedClassName(metaCode: String): String =
        "${MetaClassGenerationSettings.PACKAGE_NAME}.${classNameFromMetaCode(metaCode)}"
}
