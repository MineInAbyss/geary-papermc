package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.geary.prefabs.PrefabKey
import io.mockk.every
import io.mockk.mockk
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * Registers prefab recipe sets on the test server's real recipe manager through a [RecipeManager], the way RecipeFeature does.
 * Every test starts from the vanilla recipes and a fresh engine
 */
abstract class RecipeServerTest : RecipeTestBase() {
    private val managers = mutableListOf<RecipeManager>()

    /** What prefabs create when their recipe set has no result, null for prefabs that build no item */
    protected val prefabResults = mutableMapOf<String, ItemStack?>()

    private val itemTracking = mockk<ItemTrackingModule>(relaxed = true) {
        every { createItem(any(), any()) } answers {
            val prefab = firstArg<PrefabKey>().full
            if (prefab in prefabResults) prefabResults[prefab]?.clone() else prefabItem(prefab)
        }
    }

    @BeforeEach
    fun resetServer() {
        resetEngine()
        PaperTestServer.reset()
        resetManagerState()
        prefabResults.clear()
        recipeSets.clear()
    }

    @AfterEach
    fun closeManagers() {
        managers.forEach { runCatching { it.close() } }
        managers.clear()
    }

    private val recipeSets = mutableListOf<GearyEntity>()

    protected fun recipeSet(prefab: String, set: String) {
        val recipes = decodeSet(set)
        recipeSets += entity {
            set(recipes)
            set(PrefabKey.of(prefab))
        }
    }

    protected fun removeRecipeSets() {
        recipeSets.forEach { it.remove<SetRecipes>() }
        recipeSets.clear()
    }

    internal fun manager(autoDiscoverVanillaRecipes: Boolean = false, isStillEnabled: () -> Boolean = { true }) =
        RecipeManager(world, recipeLogger, itemTracking, autoDiscoverVanillaRecipes, isStillEnabled).also { managers += it }

    internal fun registered(autoDiscoverVanillaRecipes: Boolean = false) =
        manager(autoDiscoverVanillaRecipes).apply { registerRecipes() }

    protected fun recipeKey(prefab: String, index: Int = 0): NamespacedKey {
        val prefabKey = PrefabKey.of(prefab)
        return NamespacedKey(prefabKey.namespace, "${prefabKey.key}$index")
    }

    protected fun isOnServer(key: NamespacedKey) = RecipeRegistry.holder(key) != null

    protected fun vanilla(name: String) = NamespacedKey.minecraft(name)

    // RecipeManager keeps these across reloads in its companion, which would otherwise leak from one test into the next
    private fun resetManagerState() {
        fun field(name: String) = RecipeManager::class.java.getDeclaredField(name).apply { isAccessible = true }
        (field("removedVanilla").get(null) as RemovedRecipes).release()
        field("lastSent").set(null, null)
        field("unloadedAt").set(null, null)
    }
}
