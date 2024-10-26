package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.features.items.recipes.RecipeFeature
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackGenerator
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.GearyPluginImpl
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.brigadier.IdoCommand


internal fun IdoCommand.reload(plugin: GearyPluginImpl) = "reload" {
    requiresPermission("geary.admin.reload")
    executes {
        gearyPaper.configHolder.reload()
        with(gearyPaper.worldManager.global) {
            getAddon(Prefabs).loader.loadOrUpdatePrefabs()
            ResourcePackGenerator(this).generateResourcePack()
            plugin.features.reloadAll()
        }
    }
    "recipes" { executes { plugin.features.reload<RecipeFeature>(sender) } }
    "spawns" { executes { plugin.features.reload<SpawningFeature>(sender) } }
}
