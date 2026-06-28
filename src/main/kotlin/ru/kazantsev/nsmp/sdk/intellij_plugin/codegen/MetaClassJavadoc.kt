package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import org.jsoup.Jsoup

/**
 * Вспомогательные функции для подготовки JavaDoc сгенерированных классов и полей.
 */
object MetaClassJavadoc {

    fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
            .replace("$", "")

    fun plainDescription(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val text = Jsoup.parse(html).text()
        return text.takeIf { it.isNotEmpty() }
    }
}
