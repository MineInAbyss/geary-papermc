package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.recipes.SerializableRecipeIngredients
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:set.recipes")
class SetRecipes(
    val recipes: List<SerializableRecipeIngredients>,
    val discoverRecipes: Boolean = false,
    val group: String = "",
    val category: String = "MISC",
    val removeRecipes: List<String> = listOf(),
    val result: SerializableItemStack? = null,
)
