package ru.kazantsev.nsmp.sdk.intellij_plugin.service.sync.options.dto

import kotlinx.serialization.Serializable

@Serializable
class SrcOptionsContainer(
    val options: List<SrcOption>,
    @Suppress("unused")
    val lang: String?
)