package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal class RecipeReloadListener(private val recipes: RecipeManager) : Listener {
    // A datapack reload replaces the server's recipes with those of the datapacks alone, and has already sent players those
    @EventHandler
    fun ServerResourcesReloadedEvent.reregisterRecipes() {
        if (recipes.isLoaded) recipes.registerRecipes(forceResend = true)
    }
}
