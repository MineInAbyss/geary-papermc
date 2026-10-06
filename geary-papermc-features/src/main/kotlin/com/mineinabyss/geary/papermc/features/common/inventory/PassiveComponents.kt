package com.mineinabyss.geary.papermc.features.common.inventory

import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.entity.Player

inline fun <reified T : Any> Player.passiveComponents(): List<T> =
    inventory.toGeary()?.passiveItems?.mapNotNull { it?.get<T>() }.orEmpty()
