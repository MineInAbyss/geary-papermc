package com.mineinabyss.geary.papermc.features.items.holdsentity

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:holds_prefab")
class HoldsEntity(
    val prefabKey: PrefabKey,
    val emptiedItem: SerializableItemStack? = null
)
