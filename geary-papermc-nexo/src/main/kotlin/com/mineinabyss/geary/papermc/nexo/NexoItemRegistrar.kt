package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.nexomc.nexo.api.NexoItems
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin

// Nexo keys external items by the owning plugin so it can unregister them all on unload
internal val nexoOwner: JavaPlugin get() = JavaPlugin.getProvidingPlugin(NexoCustomBlock::class.java)

/** Hands a prefab's item-section to Nexo, which keeps it and reparses it on every item-load */
context(world: WorldScoped)
internal fun registerNexoItem(prefabKey: PrefabKey, mechanic: String, section: ConfigurationSection) {
    // Nexo saves whatever parsing filled in back to an items-file, ours has to go onto the prefab instead
    runCatching {
        NexoItems.registerExternalItem(nexoOwner, section) { parsed ->
            persistNexoAssignments(prefabKey, mechanic, parsed, world.logger)
        }
    }.onFailure { world.logger.w("Failed to register nexo item for $prefabKey: ${it.message}") }
}

/**
 * Hands every prefab-declared item to Nexo again, built off the prefab's current components.
 *
 * Nexo reparses the section it was handed, so a prefab edited since would keep registering its old one.
 * Run from Nexo's pre-load event this makes a Nexo reload pick up the edit. A prefab that lost its component
 * is not withdrawn, Nexo only unregisters per owner, so it keeps its last section until a restart.
 * Registration order is kept, a directional child resolves its parent by id while it parses
 */
context(world: WorldScoped)
internal fun registerAllNexoItems() {
    val nexo2Prefab = world.nexo2Prefab ?: return
    nexo2Prefab.prefabs.forEach { prefabKey ->
        val (mechanic, section) = nexoItemSection(prefabKey) ?: return@forEach
        registerNexoItem(prefabKey, mechanic, section)
    }
}

/** The same section the observers hand over, so both paths register the same item */
context(world: WorldScoped)
private fun nexoItemSection(prefabKey: PrefabKey): Pair<String, ConfigurationSection>? {
    val entity = prefabKey.toEntityOrNull() ?: return null
    val item = entity.get<SetItem>()?.item
    entity.get<NexoFurniture>()?.let { furniture ->
        val item = item ?: return null
        return "furniture" to furniture.toItemSection(prefabKey, item.type, item.itemModel)
    }
    entity.get<NexoCustomBlock>()?.let { block ->
        return "custom_block" to block.toItemSection(prefabKey, item?.type, item?.itemModel)
    }
    return null
}
