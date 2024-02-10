package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.messaging.info

fun Command.stats() {
    "stats" {
        action {
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
