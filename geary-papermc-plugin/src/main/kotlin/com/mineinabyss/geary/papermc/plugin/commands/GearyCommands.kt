package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeyStrings
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.brigadier.commands
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object GearyCommands {
    fun Collection<PrefabKey>.filterPrefabs(arg: String) =
        filter { it.key.startsWith(arg) || it.full.startsWith(arg) }.map { it.toString() }.take(20)

    val mobs: List<String> by lazy {
        buildList {
            addAll(listOf("custom"))
            addAll(gearyMobs.query.prefabs.getKeyStrings())
        }
    }

    fun registerCommands() {
        gearyPaper.plugin.commands {
            "geary" {
                stats()
                debug()
                items()
                mobs()
                prefabs()
                reload()
            }
        }
    }
}
