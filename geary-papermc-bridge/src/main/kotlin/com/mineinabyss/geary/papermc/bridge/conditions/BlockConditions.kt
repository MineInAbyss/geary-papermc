package com.mineinabyss.geary.papermc.bridge.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
@SerialName("geary:check.blockType")
class BlockConditions(
    val allow: Set<Material> = setOf(),
    val deny: Set<Material> = setOf()
)
