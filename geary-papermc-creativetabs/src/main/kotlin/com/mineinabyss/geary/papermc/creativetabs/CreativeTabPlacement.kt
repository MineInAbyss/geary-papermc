package com.mineinabyss.geary.papermc.creativetabs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Where this prefab's item shows in the MineInAbyss Addon creative inventory.
 * @property category Tab title when the addon groups a section into categories, defaults to the prefab's folder name
 * @property vanillaTab Key of the creative tab the item joins when the addon merges custom items into vanilla tabs, like `minecraft:natural_blocks`
 */
@Serializable
@SerialName("geary:creative_tab")
data class CreativeTabPlacement(
    val category: String? = null,
    @SerialName("vanilla_tab") val vanillaTab: String? = null,
)
