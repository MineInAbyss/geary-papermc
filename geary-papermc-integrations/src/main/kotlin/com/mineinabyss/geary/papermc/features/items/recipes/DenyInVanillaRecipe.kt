package com.mineinabyss.geary.papermc.features.items.recipes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A component to indicate that an ItemStack should not be allowed in vanilla crafting recipes.
 * Meaning if a GearyItem has a base-material of PAPER and this component, it cannot be used to craft books.
 */
@Serializable
@SerialName("looty:deny_in_vanilla_recipes")
class DenyInVanillaRecipes
