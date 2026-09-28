package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal class RecipeManager(
    private val world: Geary,
    private val logger: ComponentLogger,
    private val gearyItems: ItemTrackingModule,
    private val autoDiscoverVanillaRecipes: Boolean,
    private val isStillEnabled: () -> Boolean,
) : AutoCloseable {
    private val recipesQuery = with(world) { cache(query<SetRecipes, PrefabKey>()) }

    private val recipes = linkedMapOf<NamespacedKey, CustomRecipe>()
    private val discoverable = mutableSetOf<NamespacedKey>()
    private val fingerprints = mutableMapOf<NamespacedKey, RecipeFingerprint>()
    private val fallbackRecipes = mutableListOf<GridRecipe>()
    private var vanillaKeys = emptyList<NamespacedKey>()
    val anvilRecipes: List<AnvilCustomRecipe> field = mutableListOf<AnvilCustomRecipe>()

    var isLoaded: Boolean = false
        private set

    fun getRecipe(key: NamespacedKey): CustomRecipe? = recipes[key]

    fun isForeign(key: NamespacedKey) = key !in recipes && key.namespace != NamespacedKey.MINECRAFT

    fun isRegistered(key: NamespacedKey) = Bukkit.hasRecipe(key)

    /** [recipe] is null for a vanilla recipe */
    fun isAllowedIn(recipe: CustomRecipe?, item: ItemStack?): Boolean =
        item.isNullOrEmpty() || isAllowedInVanillaRecipes(item) || recipe?.matchesPrefabIngredient(item) == true

    fun blocksVanillaCraft(key: NamespacedKey, matrix: Array<ItemStack?>) = key in recipes || !matrix.all { isAllowedIn(null, it) }

    private fun isAllowedInVanillaRecipes(item: ItemStack?): Boolean = with(world) {
        val prefab = item?.persistentDataContainer?.decodePrefabs()?.firstOrNull() ?: return true
        entityOfOrNull(prefab)?.has<DenyInVanillaRecipes>() != true
    }

    /** Tries the recipe vanilla picked before the fallbacks, which vanilla cannot pick or may lose to an overlapping recipe */
    fun findGridRecipe(matrix: Array<ItemStack?>, vanillaPick: NamespacedKey?, isUsable: (GridRecipe) -> Boolean): GridRecipe? {
        val picked = vanillaPick?.let { recipes[it] as? GridRecipe }
        return (listOfNotNull(picked) + fallbackRecipes.filter { it !== picked }).firstOrNull { recipe ->
            isUsable(recipe) && recipe.match(matrix) != null && matrix.all { isAllowedIn(recipe, it) }
        }
    }

    fun discover(player: Player) {
        player.discoverRecipes(discoverable.filter { recipes[it]?.hasPermission(player) == true })
        player.discoverRecipes(vanillaKeys)
    }

    // Geary recipes can sit in the minecraft namespace too, they are only discovered through their own settings
    private fun findVanillaKeys(): List<NamespacedKey> {
        if (!autoDiscoverVanillaRecipes) return emptyList()
        return Bukkit.recipeIterator().asSequence().filterIsInstance<Keyed>().map { it.key }
            .filter { it.namespace == NamespacedKey.MINECRAFT && it !in recipes }
            .toList()
    }

    fun registerRecipes(forceResend: Boolean = false) {
        // Cleared until the batch completes, so a load failing midway always resends on the next one
        val previous = lastSent
        lastSent = null
        val changed = RecipeRegistry.batch {
            unregisterAll()
            val removals = requestedRemovals()
            removedVanilla.release(removals.keys).forEach { restore(it) }

            val ownKeys = recipesQuery.map { (set, prefabKey) -> set.recipes.indices.map { recipeKey(prefabKey, it) } }.flatten().toSet()
            recipesQuery.forEach { (set, prefabKey) -> registerSet(set, prefabKey, removals.keys) }
            (removals - ownKeys).forEach { (key, prefabKey) -> removeFromServer(key, prefabKey) }
            runCatching { warnPatternOverlaps() }.onFailure { it.log("Failed to check recipes for overlapping patterns") }

            val state = SentState(layout, fingerprints.toMap())
            val resend = forceResend || state != previous
            if (resend) resendToAll() else resendTo(playersJoinedSinceUnload())
            lastSent = state
            unloadedAt = null
            resend
        }
        isLoaded = true
        logger.i { "Registered ${recipes.size} recipes" + if (changed) "" else ", unchanged so not resent to players" }
        vanillaKeys = findVanillaKeys()
        Bukkit.getOnlinePlayers().forEach { discover(it) }
    }

    private fun requestedRemovals(): Map<NamespacedKey, PrefabKey> = buildMap {
        recipesQuery.forEach { (set, prefabKey) ->
            set.removeRecipes.forEach { name ->
                val key = NamespacedKey.fromString(name)
                if (key == null) logger.w { "Recipe $name removed by $prefabKey is not a valid key" }
                else put(key, prefabKey)
            }
        }
    }

    private fun RecipeRegistry.Batch.removeFromServer(key: NamespacedKey, prefabKey: PrefabKey) {
        val holder = remove(key)
        when {
            holder != null -> removedVanilla.add(key, holder)
            key !in removedVanilla -> logger.w { "Recipe $key removed by $prefabKey does not exist" }
        }
    }

    // Where vanilla picks a crafter recipe, or the one item per ingredient form a fallback registers, an overlapping recipe is blocked
    private fun RecipeRegistry.Batch.warnPatternOverlaps() {
        recipes.values.filterIsInstance<GridRecipe>().forEach { recipe ->
            val grid = when {
                // Overlaps the vanilla dye recipe for its target's material by design, which the fallback covers
                recipe is DyeCustomRecipe -> return@forEach
                recipe is ShapedCustomRecipe && !recipe.isHandCraftable -> recipe.grid.map { it?.display?.asQuantity(it.amount) }
                recipe.needsFallbackMatching -> recipe.ingredients.map { it.display.asOne() }
                else -> return@forEach
            }
            val overlapping = craftingRecipesMatching(grid) - recipe.key
            if (overlapping.isEmpty()) return@forEach
            logger.w { "Recipe ${recipe.key} registers a pattern that ${overlapping.joinToString()} also matches, vanilla picks only one recipe per grid" }
        }
    }

    private fun recipeKey(prefabKey: PrefabKey, index: Int) = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$index")

    private fun RecipeRegistry.Batch.registerSet(set: SetRecipes, prefabKey: PrefabKey, removals: Set<NamespacedKey>) {
        val setResult = buildResult(prefabKey) {
            val configured = set.result
            if (configured == null) gearyItems.createItem(prefabKey) else configured.toItemStackOrNull()
        }
        if (set.result != null && set.recipes.none { it.requiresResult }) {
            logger.w { "Recipes of $prefabKey do not use a result, so the one the set sets is ignored" }
        }
        set.recipes.forEachIndexed { i, definition ->
            when (val key = recipeKey(prefabKey, i)) {
                in removals -> return@forEachIndexed
                in recipes -> logger.w { "Recipe $key of $prefabKey is skipped, another prefab's recipe already uses that key" }
                else -> runCatching { register(key, definition, set, setResult) }.onFailure { it.log("Failed to load recipe $key") }
            }
        }
    }

    private fun RecipeRegistry.Batch.register(key: NamespacedKey, definition: RecipeDefinition, set: SetRecipes, setResult: ItemStack) {
        val configured = definition.result?.takeIf { definition.requiresResult }
        val result = configured?.let { buildResult(key) { it.toItemStackOrNull() } } ?: setResult
        val context = RecipeContext(world, key, result, set.group, set.category, set.permission?.takeUnless { it.isEmpty() }, logger)
        if (result.isEmpty && definition.requiresResult) {
            context.warn("is missing a result item")
            return
        }
        if (definition.result != null && !definition.requiresResult) context.warn("does not use a result, so the one it sets is ignored")

        val loaded = definition.load(context) ?: return
        val custom = loaded.custom
        context.warnConflictingPreserve(custom.ingredients)
        loaded.recipe?.let { add(it) }

        recipes[key] = custom
        if (set.discoverRecipes && custom.isDiscoverable) discoverable += key
        val usedResult = result.takeIf { definition.requiresResult } ?: ItemStack.empty()
        fingerprints[key] = RecipeFingerprint(
            definition.fingerprint(), usedResult, set.group, set.category, custom.ingredients.map { it.display },
        )
        if (custom is GridRecipe && custom.needsFallbackMatching) fallbackRecipes += custom
        if (custom is AnvilCustomRecipe) anvilRecipes += custom
    }

    private fun buildResult(owner: Any, build: () -> ItemStack?): ItemStack =
        runCatching(build).onFailure { it.log("Recipe $owner failed to build its result item") }.getOrNull() ?: ItemStack.empty()

    private fun playersJoinedSinceUnload(): List<Player> {
        val since = unloadedAt ?: return emptyList()
        return Bukkit.getOnlinePlayers().filter { it.lastLogin >= since }
    }

    private fun RecipeRegistry.Batch.unregisterAll() {
        recipes.keys.forEach { remove(it) }
        recipes.clear()
        fingerprints.clear()
        discoverable.clear()
        fallbackRecipes.clear()
        anvilRecipes.clear()
    }

    private fun Throwable.log(message: String) {
        logger.w { "$message, ${this.message}" }
        logger.v { stackTraceToString() }
    }

    override fun close() {
        recipesQuery.use {
            if (isLoaded) unloadedAt = System.currentTimeMillis()
            RecipeRegistry.batch {
                unregisterAll()
                // No manager follows to put back removed recipes or resend, so it happens now
                if (!isStillEnabled()) {
                    removedVanilla.release().forEach { restore(it) }
                    resendToAll()
                    lastSent = null
                }
            }
        }
    }

    // Kept across reloads, as each one creates a new manager
    private companion object {
        val removedVanilla = RemovedRecipes()
        var lastSent: SentState? = null

        // Players joining after this got a recipe book without geary's recipes
        var unloadedAt: Long? = null
    }
}
