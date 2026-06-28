package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MetaClassCommentParserTest {

    @Test
    fun extractsMetaCodeFromLineComment() {
        assertEquals("employee", MetaClassCommentParser.extractMetaCode("// nsmp:meta employee"))
    }

    @Test
    fun extractsMetaCodeWithCase() {
        assertEquals(
            "serviceCall\$request",
            MetaClassCommentParser.extractMetaCode("// nsmp:meta serviceCall\$request"),
        )
    }

    @Test
    fun returnsNullForUnrelatedComment() {
        assertNull(MetaClassCommentParser.extractMetaCode("// just a note"))
    }

    @Test
    fun findsMetaCodeOnPreviousLine() {
        assertEquals(
            "employee",
            MetaClassCommentParser.findMetaCodeNearVariableLine(
                lines = listOf(
                    "// nsmp:meta employee",
                    "def subject = utils.get('employee\$123')",
                ),
                variableLine = 1,
            ),
        )
    }

    @Test
    fun findsInlineMetaCodeOnSameLine() {
        assertEquals(
            "employee",
            MetaClassCommentParser.findMetaCodeNearVariableLine(
                lines = listOf(
                    "def subject = utils.get('employee\$123') // nsmp:meta employee",
                ),
                variableLine = 0,
            ),
        )
    }

    @Test
    fun findsMetaCodeForExplicitlyTypedVariable() {
        assertEquals(
            "employee",
            MetaClassCommentParser.findMetaCodeNearVariableLine(
                lines = listOf(
                    "import ru.naumen.core.shared.dto.ISDtObject",
                    "",
                    "// nsmp:meta employee",
                    "ISDtObject subject = utils.get('employee\$123')",
                ),
                variableLine = 3,
            ),
        )
    }

    @Test
    fun skipsBlankLineBetweenCommentAndVariable() {
        assertEquals(
            "employee",
            MetaClassCommentParser.findMetaCodeNearVariableLine(
                lines = listOf(
                    "// nsmp:meta employee",
                    "",
                    "def subject = utils.get('employee\$123')",
                ),
                variableLine = 2,
            ),
        )
    }
}
