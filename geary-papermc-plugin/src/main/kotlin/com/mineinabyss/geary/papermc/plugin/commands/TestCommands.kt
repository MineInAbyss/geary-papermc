package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.context.IdoPlayerCommandContext
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

object TestCommands {
    fun IdoCommand.test() = "test" {
        requiresPermission("geary.admin.test")
        "condition" {
            playerExecutes(Args.greedyString().named("Condition Yaml")) { yaml ->
                testCondition(yaml)
            }
        }
        "spawn" {
            fun spawningFeature() = gearyPaper.features.get<SpawningFeature>()
            fun getSpawns() = spawningFeature().spawnEntriesByName

            val spawnArg = Args.string()
                .suggests { suggestFiltering(getSpawns()?.map { it.key } ?: listOf()) }
                .named("Spawn name")

            playerExecutes(spawnArg) { spawnName ->
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

    fun IdoPlayerCommandContext.testCondition(yaml: String) {
        val decoded = gearyPaper.worldManager.global.getAddon(SerializableComponents)
            .formats["yml"]
            ?.decodeFromString(PolymorphicListAsMapSerializer.ofComponents(), yaml)
            ?: fail("Could not decode yaml")
        decoded.forEach { condition ->
            if (condition !is Condition) return@forEach
            val conditionName = condition::class.simpleName ?: return@forEach
            with(condition) {
                runCatching { ActionGroupContext(player.toGeary()).execute() }
                    .onSuccess { sender.success("Condition $conditionName passed") }
                    .onFailure { exception -> sender.error("Condition $conditionName failed:\n${exception.message}") }
            }
        }
    }
}
