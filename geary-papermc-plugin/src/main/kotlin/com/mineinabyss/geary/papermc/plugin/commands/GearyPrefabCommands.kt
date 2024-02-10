package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.typealiases.BukkitEntity
import okio.Path.Companion.toOkioPath
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

private val prefabLoader get() = prefabs.loader
private val prefabManager get() = prefabs.manager

fun Command.prefabs() {
    "prefab" {
        "reload" {
            val prefab by stringArg()
            action {
                val prefabEntity = PrefabKey.of(prefab).toEntity()
                runCatching { prefabLoader.reload(prefabEntity) }
                    .onSuccess { sender.success("Reread prefab $prefab") }
                    .onFailure { sender.error("Failed to reread prefab $prefab:\n${it.message}") }


                // Reload entities
                geary.queryManager.getEntitiesMatching(family {
                    hasRelation<InstanceOf?>(prefabEntity)
                    has<BukkitEntity>()
                }).forEach {
                    UpdateMob.recreateGearyEntity(it.get<BukkitEntity>() ?: return@forEach)
                }

                // Reload items
                geary.queryManager.getEntitiesMatching(family {
                    hasRelation<InstanceOf?>(prefabEntity)
                    has<ItemStack>()
                }).toSet()
                    .mapNotNull { it.parent }
                    .forEach { it.get<Player>()?.inventory?.toGeary()?.forceRefresh(ignoreCached = true) }
            }
        }
        "load" {
            val namespace by stringArg()
            val path by stringArg()
            action {
                // Ensure not already registered
                if (prefabManager[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                    sender.error("Prefab $namespace:$path already exists")
                    return@action
                }

                // Try to load from file
                runCatching {
                    prefabLoader.loadFromPath(
                        namespace,
                        gearyPaper.plugin.dataFolder.resolve(namespace).resolve(path).toOkioPath()
                    )
                }.onSuccess {
                    it.inheritPrefabs()
                    sender.success("Read prefab $namespace:$path")
                }.onFailure { sender.error("Failed to read prefab $namespace:$path:\n${it.message}") }
            }
        }
    }
}
