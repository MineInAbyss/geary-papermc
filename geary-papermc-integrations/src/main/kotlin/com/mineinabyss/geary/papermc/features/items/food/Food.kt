package com.mineinabyss.geary.papermc.features.items.food

import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.potion.PotionEffect

/**
 * Lets an item have custom food properties.
 *
 * @param hunger The amount of hunger this item restores.
 * @param saturation The amount of saturation this item gives.
 * @param replacement The item to replace with after consuming. If null it will subtract one from the stack.
 * @param effectChance The chance of effects being applied.
 * @param effectList The effects this item can give.
 */
@Serializable
@SerialName("looty:food")
class Food(
    val hunger: Int,
    val saturation: Double,
    val replacement: SerializableItemStack? = null,
    val effectChance: Double = 1.0,
    val effectList: List<@Serializable(with = PotionEffectSerializer::class) PotionEffect> = emptyList(),
)
