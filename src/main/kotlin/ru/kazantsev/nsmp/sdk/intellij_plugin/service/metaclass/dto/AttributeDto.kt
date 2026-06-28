package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto

import kotlinx.serialization.Serializable

/**
 * DTO атрибута метакласса, получаемого из API инсталляции.
 */
@Serializable
data class AttributeDto(
    val title: String,
    val code: String,
    val type: String,
    val hardcoded: Boolean,
    val required: Boolean,
    val requiredInInterface: Boolean,
    val unique: Boolean,
    val relatedMetaClass: String? = null,
    val description: String? = null,
) {
    fun resolvedType(): AttributeType = AttributeType.fromCode(type)
}
