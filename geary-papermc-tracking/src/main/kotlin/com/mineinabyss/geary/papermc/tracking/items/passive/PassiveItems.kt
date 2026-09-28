package com.mineinabyss.geary.papermc.tracking.items.passive

import com.mineinabyss.idofront.serialization.ItemStackSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

/** Persisted contents of a player's passive slots */
@Serializable
@SerialName("geary:passive_items")
class PassiveItems(
    val items: List<@Serializable(with = ItemStackSerializer::class) ItemStack?> = List(PassiveSlots.COUNT) { null },
    val unlockedSlots: Int = PassiveSlots.COUNT,
)
