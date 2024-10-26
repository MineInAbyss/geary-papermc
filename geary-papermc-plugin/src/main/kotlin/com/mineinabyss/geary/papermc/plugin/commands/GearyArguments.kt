package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.helpers.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.IdoCommand

class GearyArguments(
    val context: IdoCommand,
) /*: Geary by gearyPaper.worldManager.global*/ {
    fun prefabKey() = with(context) {
        ArgsMinecraft.namespacedKey().suggests {
            with(location.world.toGeary()) {
                suggest(getAddon(Prefabs).manager.keys.filter {
                    val arg = argument.lowercase()
                    it.key.startsWith(arg) || it.full.startsWith(arg)
                }.map { it.toString() })
            }
        }.map { PrefabKey.of(it.asString()) }
    }

    fun prefab() = prefabKey().map {
        location.world.toGeary().entityOfOrNull(it) ?: fail("No such prefab $it")
    }

    fun namespace() = with(context) {
        Args.word().suggests {
            suggest(this@with.plugin.dataFolder.resolve("prefabs").listFiles()?.filter {
                it.isDirectory && it.name.startsWith(suggestions.remaining.lowercase())
            }?.map { it.name } ?: emptyList())
        }
    }

    fun mob() = with(context) {
        ArgsMinecraft.namespacedKey().suggests {
            with(location.world.toGeary()) {
                suggest(getAddon(EntityTracking).query.spawnablePrefabs.getKeys().filterPrefabs(suggestions.remaining))
            }
        }.map {
            with(location.world.toGeary()) {
                entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such mob key: $it")
            }
        }
    }

    fun item() = with(context) {
        ArgsMinecraft.namespacedKey().suggests {
            with(location.world.toGeary()) {
                suggest(getAddon(ItemTracking).prefabs.getKeys().filterPrefabs(suggestions.remaining).also {
                    if (it.isEmpty()) fail("Prefab ${suggestions.remaining} not found")
                })
            }
        }.map {
            with(location.world.toGeary()) {
                entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such item key: $it")
            }
        }
    }

    fun block() = with(context) {
        ArgsMinecraft.namespacedKey().suggests {
            with(location.world.toGeary()) {
                suggest(getAddon(ItemTracking).prefabs.getKeys().filterPrefabs(suggestions.remaining))
            }
        }.map {
            with(location.world.toGeary()) {
                entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such block key: $it")
            }
        }
    }
}

val IdoCommand.GearyArgs get() = GearyArguments(this)
