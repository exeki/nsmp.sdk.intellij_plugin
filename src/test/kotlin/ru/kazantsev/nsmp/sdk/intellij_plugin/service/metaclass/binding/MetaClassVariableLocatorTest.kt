package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding

import kotlin.test.Test
import kotlin.test.assertEquals

class MetaClassVariableLocatorTest {

    @Test
    fun findPrecedingVariableDeclarationUsesClosestMatch() {
        val metaCode = MetaClassCommentParser.findMetaCodeNearVariableLine(
            lines = listOf(
                "// nsmp:meta employee",
                "ISDtObject subject = utils.get('employee\$123')",
                "subject",
            ),
            variableLine = 1,
        )
        assertEquals("employee", metaCode)
    }
}
