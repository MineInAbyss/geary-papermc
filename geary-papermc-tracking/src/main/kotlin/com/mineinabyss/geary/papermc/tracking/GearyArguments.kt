package com.mineinabyss.geary.papermc.tracking

import com.mineinabyss.geary.papermc.gearyPaper
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
            suggestRanked(getAddon(Prefabs).prefabs.keys.map { it.toString() })
        }
    }.map { PrefabKey.of(it.asString()) }

    fun prefab() = prefabKey().map {
        location.world.toGeary().entityOfOrNull(it) ?: fail("No such prefab $it")
    }

    fun namespace() = Args.word().suggests {
        suggestRanked(gearyPaper.dataFolder.resolve("prefabs").listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList())
    }

    fun mob() = ArgsMinecraft.namespacedKey().suggests {
        with(location.world.toGeary()) {
            suggestRanked(getAddon(EntityTracking).query.spawnablePrefabs.getKeys().map { it.toString() }, limit = 20)
        }
    }.map {
        with(location.world.toGeary()) {
            entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such mob key: $it")
        }
    }

    fun item() = ArgsMinecraft.namespacedKey().suggests {
        with(location.world.toGeary()) {
            suggestRanked(getAddon(ItemTracking).prefabs.getKeys().map { it.toString() }, limit = 20)
        }
    }.map {
        with(location.world.toGeary()) {
            entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such item key: $it")
        }
    }

    fun block() = ArgsMinecraft.namespacedKey().suggests {
        with(location.world.toGeary()) {
            suggestRanked(getAddon(ItemTracking).prefabs.getKeys().map { it.toString() }, limit = 20)
        }
    }.map {
        with(location.world.toGeary()) {
            entityOfOrNull(PrefabKey.of(it.asString())) ?: fail("No such block key: $it")
        }
    }
}

val Args.geary get() = GearyArgs