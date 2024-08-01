package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import org.bukkit.NamespacedKey

/**
 * This system is implemented separate from idofront recipes since they are handled differently by Minecraft.
 */
fun GearyModule.trackPotionMixes() = observe<OnSet>()
    .involving(query<SetPotionMixes, PrefabKey>())
    .exec { (potionMixes, prefabKey) ->
        val result = potionMixes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)

        if (result != null) {
            potionMixes.potionmixes.forEachIndexed { i, potionmix ->
                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                gearyPaper.plugin.server.potionBrewer.removePotionMix(key)
                gearyPaper.plugin.server.potionBrewer.addPotionMix(potionmix.toPotionMix(key, result))
            }
        } else gearyPaper.logger.w { "PotionMix $prefabKey is missing result item" }
    }
