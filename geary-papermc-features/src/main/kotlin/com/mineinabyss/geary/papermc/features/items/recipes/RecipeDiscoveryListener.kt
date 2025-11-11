package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.gearyPaper
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class RecipeDiscoveryListener(
    val context: RecipeContext,
) : Listener {
    @EventHandler
    fun PlayerJoinEvent.showRecipesOnJoin() {
        val discoveredRecipes = context.recipes

        player.discoverRecipes(discoveredRecipes)
        if (gearyPaper.config.items.autoDiscoverVanillaRecipes)
            player.discoverRecipes(Bukkit.recipeIterator().asSequence().filterIsInstance<Keyed>()
                .filter { it.key.namespace == "minecraft" && it.key !in discoveredRecipes }.map { it.key }.toList()
            )
    }
}
