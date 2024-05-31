package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.recipes.PotionMixRecipeIngredients
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:set.potion_mixes")
class SetPotionMixes(
    val result: SerializableItemStack? = null,
    val potionmixes: List<PotionMixRecipeIngredients> = emptyList(),
)
