package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.IdoCommand

class GearyArguments(val context: IdoCommand) {
    fun prefab() = with(context) {
        ArgsMinecraft.namespacedKey().suggests {
            suggest(prefabs.manager.keys.filter {
                val arg = argument.lowercase()
                it.key.startsWith(arg) || it.full.startsWith(arg)
            }.map { it.toString() })
        }
    }
}

val IdoCommand.GearyArgs get() = GearyArguments(this)
