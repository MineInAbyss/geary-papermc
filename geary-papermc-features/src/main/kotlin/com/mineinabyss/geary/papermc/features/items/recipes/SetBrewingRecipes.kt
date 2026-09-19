package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.recipes.BrewingRecipeIngredients
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:set.brewing_recipes")
class SetBrewingRecipes(
    val result: SerializableItemStack? = null,
    val recipes: List<BrewingRecipeIngredients> = emptyList(),
)
