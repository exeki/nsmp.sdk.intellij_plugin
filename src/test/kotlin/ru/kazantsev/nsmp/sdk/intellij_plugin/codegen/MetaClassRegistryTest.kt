package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.AttributeDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import kotlin.test.Test
import kotlin.test.assertEquals

class MetaClassRegistryTest {

    @Test
    fun parentChainCollectsAncestorsFromRootToChild() {
        val parent = MetaClassDto(
            title = "Abstract BO",
            fullCode = "abstractBO",
            classCode = "abstractBO",
            hasResponsible = false,
            hasWorkflow = false,
            hardcoded = true,
            attributes = listOf(stringAttribute("title")),
        )
        val child = MetaClassDto(
            title = "Employee",
            fullCode = "employee",
            classCode = "employee",
            parent = "abstractBO",
            hasResponsible = false,
            hasWorkflow = false,
            hardcoded = false,
            attributes = listOf(stringAttribute("lastName")),
        )

        val chain = MetaClassRegistry(listOf(parent, child)).parentChain(child)
        assertEquals(listOf("abstractBO", "employee"), chain.map { it.fullCode })
    }

    private fun stringAttribute(code: String): AttributeDto =
        AttributeDto(
            title = code,
            code = code,
            type = "string",
            hardcoded = false,
            required = false,
            requiredInInterface = false,
            unique = false,
        )
}
