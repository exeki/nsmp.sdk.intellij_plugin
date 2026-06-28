package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.dto

import kotlinx.serialization.Serializable

/**
 * DTO группы атрибутов метакласса.
 */
@Serializable
data class AttributeGroupDto(
    val title: String?,
    val code: String,
    val attributes: List<String> = emptyList(),
)
