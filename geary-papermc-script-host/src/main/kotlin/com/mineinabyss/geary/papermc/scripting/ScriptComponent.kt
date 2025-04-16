package com.mineinabyss.geary.papermc.scripting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("geary:scripts")
value class Scripts(
    val scripts: List<ScriptRef>,
) {
    @JvmInline
    @Serializable
    value class ScriptRef(val path: String)
}
