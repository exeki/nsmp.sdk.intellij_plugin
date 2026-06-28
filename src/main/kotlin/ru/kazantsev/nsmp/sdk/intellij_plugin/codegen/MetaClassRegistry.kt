package ru.kazantsev.nsmp.sdk.intellij_plugin.codegen

import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.AttributeDto
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto.MetaClassDto

/**
 * Индекс метаклассов по fullCode для разрешения ссылочных типов при генерации.
 */
class MetaClassRegistry(metaClasses: Collection<MetaClassDto>) {

    private val byFullCode: Map<String, MetaClassDto> =
        metaClasses.associateBy { it.fullCode }

    fun contains(metaCode: String?): Boolean =
        metaCode != null && byFullCode.containsKey(metaCode)

    fun get(metaCode: String): MetaClassDto? = byFullCode[metaCode]

    fun parentChain(metaClass: MetaClassDto): List<MetaClassDto> {
        val chain = mutableListOf<MetaClassDto>()
        var current: MetaClassDto? = metaClass
        while (current != null) {
            chain.add(0, current)
            current = current.parent?.let(::get)
        }
        return chain
    }

    fun ownAttributes(metaClass: MetaClassDto): List<AttributeDto> = metaClass.attributes

    fun metaClassType(metaCode: String): com.squareup.javapoet.ClassName =
        if (contains(metaCode)) {
            com.squareup.javapoet.ClassName.get(
                MetaClassGenerationSettings.PACKAGE_NAME,
                MetaClassNaming.classNameFromMetaCode(metaCode),
            )
        } else {
            PlatformTypes.objectType
        }
}
