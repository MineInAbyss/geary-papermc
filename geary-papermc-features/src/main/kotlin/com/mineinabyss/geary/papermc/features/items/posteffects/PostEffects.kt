package com.mineinabyss.geary.papermc.features.items.posteffects

import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key

@Serializable
@SerialName("geary:post_effects")
@JvmInline
value class PostEffects(val effects: List<@Serializable(KeySerializer::class) Key>) {
    init {
        require(effects.isNotEmpty()) { "PostEffects must list at least one effect" }
    }
}
