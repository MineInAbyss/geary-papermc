package com.mineinabyss.geary.papermc.features.items.recipes

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

internal class RecipeDiscoveryListener(private val recipes: RecipeManager) : Listener {
    @EventHandler
    fun PlayerJoinEvent.discoverOnJoin() {
        recipes.discover(player)
    }
}
