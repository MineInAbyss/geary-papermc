package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.context.IdoPlayerCommandContext
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

fun IdoCommand.testCommands() = "test" {
    requiresPermission("geary.admin.test")
    "condition" {
        playerExecutes(Args.greedyString()) { yaml ->
            test(yaml)
        }
    }
}

fun IdoPlayerCommandContext.test(yaml: String) {
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
