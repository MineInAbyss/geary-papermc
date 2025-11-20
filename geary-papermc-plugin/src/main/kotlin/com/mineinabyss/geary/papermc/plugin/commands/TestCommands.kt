package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.context.IdoPlayerCommandContext
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

val TestingFeature = feature("testing") {
    mainCommand {
        "test" {
            permission = "geary.admin.test"
            fun IdoPlayerCommandContext.executeYaml(yaml: String) {
                val decoded = runCatching {
                    gearyPaper.worldManager.global.getAddon(SerializableComponents)
                        .formats.getFormat("yml")
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
            "execute" {
                executes.asPlayer().args("Action or condition Yaml" to Args.greedyString()) { yaml ->
                    executeYaml(yaml)
                }
            }
        }
    }
}
