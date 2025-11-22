package com.mineinabyss.geary.papermc.tracking

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.getAddon
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
import com.mineinabyss.idofront.commands.brigadier.suggests

object GearyArgs {
    fun prefabKey() = ArgsMinecraft.namespacedKey().suggests {
        with(location.world.toGeary()) {
            suggest(getAddon(Prefabs).prefabs.keys.filter {
                val arg = argument.lowercase()
                it.key.startsWith(arg) || it.full.startsWith(arg)
            }.map { it.toString() })
        }
    }.map { PrefabKey.of(it.asString()) }

    fun prefab() = prefabKey().map {
        location.world.toGeary().entityOfOrNull(it) ?: fail("No such prefab $it")
    }

    fun namespace() = Args.word().suggests {
        suggest(gearyPaper.plugin.dataFolder.resolve("prefabs").listFiles()?.filter {
            it.isDirectory && it.name.startsWith(suggestions.remaining.lowercase())
        }?.map { it.name } ?: emptyList())
    }

    fun mob() = ArgsMinecraft.namespacedKey().suggests {
        with(location.world.toGeary()) {
            suggest(getAddon(EntityTracking).query.spawnablePrefabs.getKeys().filterPrefabs(suggestions.remaining))
        }
    }.map {
        with(location.world.toGeary()) {
            entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such mob key: $it")
        }
    }

    fun item() = ArgsMinecraft.namespacedKey().suggests {
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

    fun block() = ArgsMinecraft.namespacedKey().suggests {
        with(location.world.toGeary()) {
            suggest(getAddon(ItemTracking).prefabs.getKeys().filterPrefabs(suggestions.remaining))
        }
    }.map {
        with(location.world.toGeary()) {
            entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such block key: $it")
        }
    }
}

internal fun Collection<PrefabKey>.filterPrefabs(arg: String): List<String> =
    filter { it.key.startsWith(arg) || it.full.startsWith(arg) }.map { it.toString() }.take(20)

val Args.geary get() = GearyArgs