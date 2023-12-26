package com.mineinabyss.geary.papermc.bridge.actions.components

import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.potion.PotionEffect

@Serializable
@SerialName("geary:potionEffects")
class PotionEffects(
    val effects: List<@Serializable(with = PotionEffectSerializer::class) PotionEffect>
)
