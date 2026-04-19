package ru.kazantsev.nsmp.sdk.intellij_plugin.services.sync.options.dto

import kotlinx.serialization.Serializable

@Serializable
class SrcOptionsContainer(
    val options: List<SrcOption>,
    val lang: String?
)