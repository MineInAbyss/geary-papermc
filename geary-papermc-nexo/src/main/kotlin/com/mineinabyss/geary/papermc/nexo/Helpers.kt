package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.nexomc.nexo.api.NexoBlocks
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

// Resolves to null when Nexo is missing, where the feature never loaded
internal val WorldScoped.nexo2Prefab get() = world.scope.getOrNull(NexoFeature)

private fun WorldScoped.prefabKeyOf(itemId: String?) = itemId?.let { nexo2Prefab?.get(it) }

val Block.prefabKey: PrefabKey? get() = withGeary { prefabKeyOf(NexoBlocks.customBlockMechanic(it)?.itemID) }

fun Block.toGearyOrNull() = withGeary { entityOfOrNull(prefabKeyOf(NexoBlocks.customBlockMechanic(it)?.itemID)) }

context(world: WorldScoped)
val BlockData.prefabKey: PrefabKey?
    get() = world.prefabKeyOf(NexoBlocks.customBlockMechanic(this)?.itemID)

context(world: WorldScoped)
fun BlockData.toGearyOrNull() = world.entityOfOrNull(prefabKey)
