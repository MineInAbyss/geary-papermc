package com.mineinabyss.geary.papermc.features.items.recipes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Keeps an item out of recipes that only ask for its base material or a tag, so a PAPER based item cannot craft books.
 * Recipes with an ingredient naming its prefab still accept it
 */
@Serializable
@SerialName("geary:deny_in_vanilla_recipes")
class DenyInVanillaRecipes
