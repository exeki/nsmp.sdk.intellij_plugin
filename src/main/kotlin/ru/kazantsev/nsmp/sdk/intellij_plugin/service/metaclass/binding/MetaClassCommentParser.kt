package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import com.intellij.openapi.editor.Document
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import ru.kazantsev.nsmp.sdk.intellij_plugin.codegen.MetaClassGenerationSettings

/**
 * Извлекает привязку переменная → код метакласса из комментария `// nsmp:meta ...`.
 */
object MetaClassCommentParser {

    private val META_COMMENT_PATTERN =
        Regex("""${MetaClassGenerationSettings.META_COMMENT_PREFIX}\s+(\S+)""")

    fun findMetaCode(file: GroovyFile, variableName: String): String? {
        val variable = PsiTreeUtil.findChildrenOfType(file, GrVariable::class.java)
            .firstOrNull { it.name == variableName }
            ?: return null
        return findMetaCodeForVariable(file, variable)
    }

    fun findMetaCodeForVariable(file: GroovyFile, variable: GrVariable): String? {
        val document = file.viewProvider.document ?: return null
        val anchor = variable.nameIdentifier ?: variable
        val variableLine = document.getLineNumber(anchor.textRange.startOffset)
        val lines = (0 until document.lineCount).map { line ->
            document.charsSequence.subSequence(
                document.getLineStartOffset(line),
                document.getLineEndOffset(line),
            ).toString()
        }
        return findMetaCodeNearVariableLine(lines, variableLine)
    }

    fun findMetaCodeForReference(file: GroovyFile, reference: GrReferenceExpression): String? {
        val variable = MetaClassVariableLocator.locate(file, reference) ?: return null
        return findMetaCodeForVariable(file, variable)
    }

    internal fun findMetaCodeNearVariableLine(lines: List<String>, variableLine: Int): String? {
        if (variableLine !in lines.indices) return null

        extractMetaCode(lines[variableLine])?.let { return it }

        var line = variableLine - 1
        while (line >= 0) {
            val lineText = lines[line]
            if (lineText.isBlank()) {
                line--
                continue
            }
            if (isCommentLine(lineText)) {
                extractMetaCode(lineText)?.let { return it }
                return null
            }
            break
        }
        return null
    }

    fun extractMetaCode(text: String): String? =
        META_COMMENT_PATTERN.find(text.trim())?.groupValues?.get(1)

    private fun isCommentLine(lineText: String): Boolean {
        val trimmed = lineText.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")
    }
}
