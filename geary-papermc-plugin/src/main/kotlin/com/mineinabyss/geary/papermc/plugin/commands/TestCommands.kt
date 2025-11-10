package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.context.IdoPlayerCommandContext
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

object TestCommands {
    fun IdoCommand.test() = "test" {
        permission = "geary.admin.test"
        "execute" {
            executes.asPlayer().args("Action or condition Yaml" to Args.greedyString()) { yaml ->
                executeYaml(yaml)
            }
        }
        "spawn" {
            fun spawningFeature() = gearyPaper.features.get<SpawningFeature>()
            fun getSpawns() = spawningFeature().spawnEntriesByName

            val spawnArg = Args.string()
                .suggests { suggestFiltering(getSpawns()?.map { it.key } ?: listOf()) }

            executes.asPlayer().args("Spawn name" to spawnArg) { spawnName ->
                val spawn = getSpawns()?.get(spawnName) ?: fail("Could not find spawn named $spawnName")
                val spawner = spawningFeature().spawnTask?.mobSpawner ?: fail("Mob spawn task not enabled")
                runCatching { spawner.checkSpawnConditions(spawn, player.location) }
                    .onSuccess {
                        if (it) sender.success("Conditions for $spawnName passed") else sender.error("Conditions for $spawnName failed")
                    }
                    .onFailure { sender.error("Conditions for $spawnName failed:\n${it.message}") }
            }
        }
    }

    private fun IdoPlayerCommandContext.executeYaml(yaml: String) {
        val decoded = runCatching {
            gearyPaper.worldManager.global.getAddon(SerializableComponents)
                .formats["yml"]
                ?.decodeFromString(PolymorphicListAsMapSerializer.ofComponents(), yaml)
                ?: fail("Could not decode yaml")
        }.getOrElse { fail("Could not decode yaml:\n${it.message}") }
        decoded.forEach { comp ->
            val className = comp::class.simpleName ?: return@forEach
            when (comp) {
                is Condition -> {
                    with(comp) {
                        runCatching { ActionGroupContext(player.toGeary()).execute() }
                            .onSuccess { if (it) return sender.success("Condition $className passed") else sender.error("Condition $className failed without extra info") }
                            .onFailure { sender.error("Condition $className failed:\n${it.message}") }
                    }
                }

                is Action -> {
                    runCatching { comp.execute(ActionGroupContext(player.toGeary())) }
                        .onSuccess { sender.success("Action $className succeeded") }
                        .onFailure { sender.error("Action $className failed:\n${it.message}") }
                }
            }
        }
    }
}
