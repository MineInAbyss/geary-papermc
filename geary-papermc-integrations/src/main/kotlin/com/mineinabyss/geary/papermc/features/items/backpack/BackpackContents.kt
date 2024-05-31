package com.mineinabyss.geary.papermc.features.items.backpack

import com.mineinabyss.idofront.serialization.ItemStackSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("geary:backpack_contents")
class BackpackContents(val contents: List<@Serializable(ItemStackSerializer::class) ItemStack> = emptyList())
