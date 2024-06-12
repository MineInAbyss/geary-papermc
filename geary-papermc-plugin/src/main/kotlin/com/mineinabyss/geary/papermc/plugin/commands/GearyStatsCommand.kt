package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.messaging.info

fun IdoRootCommand.stats() {
    "stats" {
        executes {
            val tempEntity = entity()

            sender.info(
                """
                |Archetype count: ${archetypes.queryManager.archetypeCount}
                |Next entity ID: ${tempEntity.id}
                |""".trimMargin()
            )

            tempEntity.removeEntity()
        }
    }
}
