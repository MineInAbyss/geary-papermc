package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.plugin.GearyPluginImpl
import com.mineinabyss.geary.papermc.plugin.commands.TestCommands.test
import com.mineinabyss.geary.papermc.plugin.commands.mobs.mobs
import com.mineinabyss.idofront.commands.brigadier.commands

internal fun GearyPluginImpl.registerGearyCommands() = commands {
    "geary" {
        mobs()
        prefabs()
        debug()
        test()
    }
}
