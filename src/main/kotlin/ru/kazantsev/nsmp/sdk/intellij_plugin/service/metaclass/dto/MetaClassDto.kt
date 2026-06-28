package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto

import kotlinx.serialization.Serializable

/**
 * DTO метакласса NSMP со списком атрибутов и метаданными.
 */
@Serializable
data class MetaClassDto(
    val title: String,
    val fullCode: String,
    val caseCode: String? = null,
    val classCode: String,
    val parent: String? = null,
    val description: String? = null,
    val hasResponsible: Boolean,
    val hasWorkflow: Boolean,
    val hardcoded: Boolean,
    val children: List<String> = emptyList(),
    val attributes: List<AttributeDto> = emptyList(),
    val attributeGroups: List<AttributeGroupDto> = emptyList(),
)
