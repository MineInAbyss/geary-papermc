package com.mineinabyss.geary.papermc.features.items.recipes

import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.DyeRecipe
import net.minecraft.world.item.crafting.RecipeCache
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.CrafterBlock
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.inventory.CraftRecipe
import org.bukkit.craftbukkit.util.CraftNamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.recipe.CraftingBookCategory
import java.lang.ref.WeakReference
import net.minecraft.world.item.crafting.CraftingRecipe as MinecraftCraftingRecipe
import net.minecraft.world.item.crafting.Recipe as MinecraftRecipe
import net.minecraft.world.item.crafting.RecipeManager as MinecraftRecipeManager

/**
 * Edits the server's recipes through NMS, as Paper's API finalizes and resends every recipe to every player on each single change.
 * Changes made in a [batch] are finalized once at its end, and only reach players it asks to resend to
 */
internal object RecipeRegistry {
    fun <T> batch(block: Batch.() -> T): T {
        val batch = Batch(MinecraftServer.getServer())
        val result = try {
            batch.block()
        } catch (e: Throwable) {
            // Whatever the batch got through is live on the server already, clients have to see it too
            batch.resendToAll()
            runCatching { batch.finish() }.onFailure(e::addSuppressed)
            throw e
        }
        batch.finish()
        return result
    }

    fun holder(key: NamespacedKey): RecipeHolder<*>? = MinecraftServer.getServer().recipeManager.recipes.byKey(key.toResourceKey())

    /** Compared to tell whether clients hold stale recipe display ids, which are positions in the server's recipe map */
    data class Layout(val ids: List<ResourceKey<MinecraftRecipe<*>>>)

    class Batch(private val server: MinecraftServer) {
        private val manager = server.recipeManager
        private var modified = false
        private var resendsToAll = false
        private val pendingResends = mutableSetOf<Player>()

        val layout: Layout get() = Layout(manager.recipes.values().map { it.id() })

        /** [grid] fills a 3x3 grid row by row */
        fun craftingRecipesMatching(grid: List<ItemStack?>): List<NamespacedKey> {
            val slots = List(RecipeMatcher.GRID_WIDTH * RecipeMatcher.GRID_WIDTH) { CraftItemStack.asNMSCopy(grid.getOrNull(it)) }
            val input = CraftingInput.of(RecipeMatcher.GRID_WIDTH, RecipeMatcher.GRID_WIDTH, slots)
            return manager.recipes.getRecipesFor(RecipeType.CRAFTING, input, server.overworld())
                .map { CraftNamespacedKey.fromMinecraft(it.id().identifier()) }
                .toList()
        }

        fun add(recipe: Recipe) {
            val holder = recipe.toHolder()
            manager.recipes.removeRecipe(holder.id())
            manager.recipes.addRecipe(holder)
            modified = true
        }

        fun remove(key: NamespacedKey): RecipeHolder<*>? {
            val id = key.toResourceKey()
            val holder = manager.recipes.byKey(id) ?: return null
            manager.recipes.removeRecipe(id)
            modified = true
            return holder
        }

        fun restore(holder: RecipeHolder<*>) {
            if (manager.recipes.byKey(holder.id()) != null) return
            // RecipeMap.addRecipe flags a holder as added at runtime, which would let a vanilla recipe win over overlapping plugin recipes
            manager.recipes.byType.put(holder.value().type, holder)
            manager.recipes.byKey[holder.id()] = holder
            modified = true
        }

        fun resendToAll() {
            resendsToAll = true
        }

        fun resendTo(players: Collection<Player>) {
            pendingResends += players
        }

        fun finish() {
            if (modified) {
                // The overload without flags would also reload every player's advancements from disk, losing unsaved progress
                manager.finalizeRecipeLoading(server.worldData.enabledFeatures())
                // What /recipe give * hands out, which only the overload without flags refreshes
                learnableRecipes.set(manager, manager.recipes.values().filterNot { it.value().isSpecial })
                // Crafters only drop their cached recipes once the manager itself is replaced, which edits made in place never do
                cachedRecipeManager.set(crafterRecipeCache.get(null), WeakReference<MinecraftRecipeManager>(null))
            }
            if (resendsToAll) Bukkit.updateRecipes()
            else pendingResends.filter { it.isOnline }.forEach(::resend)
        }

        private fun resend(player: Player) {
            val handle = (player as CraftPlayer).handle
            handle.connection.send(ClientboundUpdateRecipesPacket(manager.synchronizedItemProperties, manager.synchronizedStonecutterRecipes))
            handle.recipeBook.sendInitialRecipeBook(handle)
        }
    }
}

private val learnableRecipes = MinecraftRecipeManager::class.java.getDeclaredField("learnableRecipes").apply { isAccessible = true }
private val crafterRecipeCache = CrafterBlock::class.java.getDeclaredField("RECIPE_CACHE").apply { isAccessible = true }
private val cachedRecipeManager = RecipeCache::class.java.getDeclaredField("cachedRecipeManager").apply { isAccessible = true }

internal fun Recipe.toHolder(): RecipeHolder<*> =
    CraftRecipe.fromBukkitRecipe(this)?.toMinecraftRecipe() ?: error("${this::class.simpleName} recipes cannot be registered")

// The result is always rebuilt from the target, so the template only has to be a valid item
internal fun dyeRecipe(
    key: NamespacedKey,
    target: RecipeIngredient,
    dye: RecipeIngredient,
    group: String,
    category: CraftingBookCategory,
): Recipe = DyeRecipe(
    MinecraftRecipe.CommonInfo(true),
    MinecraftCraftingRecipe.CraftingBookInfo(CraftRecipe.getCategory(category), group),
    CraftRecipe.toIngredient(target.choice, true),
    CraftRecipe.toIngredient(dye.choice, true),
    CraftItemStack.asTemplate(ItemStack.of(target.display.type)),
).toBukkitRecipe(key)

private fun NamespacedKey.toResourceKey() = CraftNamespacedKey.toResourceKey(Registries.RECIPE, this)
