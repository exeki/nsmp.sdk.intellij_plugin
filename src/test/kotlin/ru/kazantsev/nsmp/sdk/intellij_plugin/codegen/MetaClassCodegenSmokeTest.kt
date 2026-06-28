package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.AttributeDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto
import kotlin.test.Test

class MetaClassCodegenSmokeTest {

    @Test
    fun generatesTypeSpecForSimpleMetaClass() {
        val metaClass = MetaClassDto(
            title = "Employee",
            fullCode = "employee",
            classCode = "employee",
            hasResponsible = false,
            hasWorkflow = false,
            hardcoded = false,
            attributes = listOf(
                AttributeDto(
                    title = "Last name",
                    code = "lastName",
                    type = "string",
                    hardcoded = false,
                    required = false,
                    requiredInInterface = false,
                    unique = false,
                ),
            ),
        )
        val registry = MetaClassRegistry(listOf(metaClass))
        MetaClassTypeSpecFactory(registry).create(metaClass)
    }
}
