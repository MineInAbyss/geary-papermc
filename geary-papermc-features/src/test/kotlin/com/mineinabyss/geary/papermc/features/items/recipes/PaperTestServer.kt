package com.mineinabyss.geary.papermc.features.items.recipes

import com.google.common.util.concurrent.MoreExecutors
import com.mineinabyss.idofront.services.SerializableItemStackService
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import net.minecraft.SharedConstants
import net.minecraft.commands.Commands
import net.minecraft.resources.RegistryDataLoader
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.RegistryLayer
import net.minecraft.server.ReloadableServerResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.ServerPacksSource
import net.minecraft.server.packs.resources.MultiPackResourceManager
import net.minecraft.server.permissions.PermissionSet
import net.minecraft.tags.TagLoader
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeMap
import net.minecraft.world.item.crafting.RecipeType
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.craftbukkit.CraftRegistry
import org.bukkit.craftbukkit.inventory.CraftItemFactory
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import java.util.logging.Logger
import net.minecraft.world.item.crafting.RecipeManager as MinecraftRecipeManager

/**
 * Loads the vanilla registries, tags, item components and recipes the way a starting server does, so tests use real Paper item stacks.
 * MockBukkit is not used as its data components are a plain map without item defaults.
 * The Minecraft server is a mock handing out the real recipe manager, which is all RecipeRegistry needs
 */
object PaperTestServer {
    private val started by lazy { bootstrap() }

    lateinit var recipeManager: MinecraftRecipeManager
        private set

    private lateinit var vanillaRecipes: List<RecipeHolder<*>>

    val onlinePlayers = mutableListOf<Player>()

    /** Times recipes were resent to every player */
    var recipeUpdates = 0
        private set

    fun start() = started

    fun reset() {
        start()
        val recipes = RecipeMap.createEmpty()
        vanillaRecipes.forEach {
            recipes.byType.put(it.value().type, it)
            recipes.byKey[it.id()] = it
        }
        recipeManager.recipes = recipes
        recipeManager.finalizeRecipeLoading(FeatureFlags.VANILLA_SET)
        onlinePlayers.clear()
        recipeUpdates = 0
    }

    fun recipe(key: NamespacedKey): Recipe? = RecipeRegistry.holder(key)?.toBukkitRecipe()

    fun vanillaCraftingPick(matrix: Array<ItemStack?>): Recipe? {
        val width = if (matrix.size == 4) 2 else 3
        val input = CraftingInput.of(width, width, matrix.map { CraftItemStack.asNMSCopy(it) })
        return recipeManager.getRecipeFor(RecipeType.CRAFTING, input, MinecraftServer.getServer().overworld())
            .map { it.toBukkitRecipe() }.orElse(null)
    }

    private fun bootstrap() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()

        val executor = MoreExecutors.directExecutor()
        val resources = MultiPackResourceManager(PackType.SERVER_DATA, listOf(ServerPacksSource.createVanillaPackSource().fullResources()))
        val layers = RegistryLayer.createRegistryAccess()
        val staticTags = TagLoader.loadTagsForExistingRegistries(resources, layers.getLayer(RegistryLayer.STATIC), ReloadableRegistrarEvent.Cause.INITIAL)
        val worldLookups = TagLoader.buildUpdatedLookups(layers.getAccessForLoading(RegistryLayer.WORLD), staticTags)
        val world = RegistryDataLoader.load(resources, worldLookups, RegistryDataLoader.WORLD_REGISTRIES, executor).join()
        val dimensionLookups = worldLookups + world.listRegistries().toList()
        val dimensions = RegistryDataLoader.load(resources, dimensionLookups, RegistryDataLoader.DIMENSION_REGISTRIES, executor).join()
        val loaded = layers.replaceFrom(RegistryLayer.WORLD, world, dimensions)

        val serverResources = ReloadableServerResources.loadResources(
            resources, loaded, staticTags, FeatureFlags.VANILLA_SET, Commands.CommandSelection.DEDICATED,
            PermissionSet.ALL_PERMISSIONS, executor, executor,
        ).join()
        serverResources.updateComponentsAndStaticRegistryTags()
        CraftRegistry.setMinecraftRegistry(loaded.compositeAccess().freeze())

        recipeManager = serverResources.recipeManager
        recipeManager.finalizeRecipeLoading(FeatureFlags.VANILLA_SET)
        vanillaRecipes = recipeManager.recipes.values().toList()
        installMinecraftServer()

        Bukkit.setServer(mockk<Server>(relaxed = true) {
            every { logger } returns Logger.getLogger("PaperTestServer")
            every { itemFactory } returns CraftItemFactory.instance()
            every { unsafe } returns CraftMagicNumbers.INSTANCE
            every { servicesManager.load(SerializableItemStackService::class.java) } returns TestItemProviders
            every { onlinePlayers } answers { this@PaperTestServer.onlinePlayers.toList() }
            every { hasRecipe(any()) } answers { RecipeRegistry.holder(firstArg()) != null }
            every { getRecipe(any()) } answers { recipe(firstArg()) }
            every { recipeIterator() } answers { recipeManager.recipes.values().map { it.toBukkitRecipe() }.iterator() }
            every { updateRecipes() } answers { recipeUpdates++ }
        })
    }

    private fun installMinecraftServer() {
        val server = mockk<MinecraftServer>(relaxed = true) {
            every { recipeManager } answers { this@PaperTestServer.recipeManager }
            every { worldData.enabledFeatures() } returns FeatureFlags.VANILLA_SET
        }
        MinecraftServer::class.java.getDeclaredField("SERVER").apply { isAccessible = true }.set(null, server)
    }
}
