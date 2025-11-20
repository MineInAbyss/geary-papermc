package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.GearyPaperConfig
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class RecipeDiscoveryListener(
    val context: RecipeManager,
    val config: GearyPaperConfig,
) : Listener {
    @EventHandler
    fun PlayerJoinEvent.showRecipesOnJoin() {
        val discoveredRecipes = context.loadedRecipes.discoverable

        player.discoverRecipes(discoveredRecipes)

        if (config.items.autoDiscoverVanillaRecipes) {
            player.discoverRecipes(Bukkit.recipeIterator().asSequence().filterIsInstance<Keyed>()
                .filter { it.key.namespace == "minecraft" && it.key !in discoveredRecipes }.map { it.key }.toList()
            )
        }
    }
}
