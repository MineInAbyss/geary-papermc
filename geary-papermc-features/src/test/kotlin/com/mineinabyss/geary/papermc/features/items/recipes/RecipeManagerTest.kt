package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ShapedRecipe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecipeManagerTest : RecipeServerTest() {
    private val bedrockRecipe = "{ type: shaped, items: { B: minecraft:bedrock }, shape: [ B ] }"

    private fun datapackReload(manager: RecipeManager) =
        RecipeReloadListener(manager).run { ServerResourcesReloadedEvent(ServerResourcesReloadedEvent.Cause.COMMAND).reregisterRecipes() }

    @Nested
    inner class Registering {
        @Test
        fun `registers every recipe of a set under its prefab's key and index`() {
            recipeSet(
                "test:knife",
                """
                recipes:
                  - $bedrockRecipe
                  - { type: furnace, input: minecraft:bedrock }
                  - { type: anvil, input: minecraft:iron_pickaxe, material: minecraft:bedrock }
                """
            )
            val manager = registered()

            PaperTestServer.recipe(recipeKey("test:knife", 0)).shouldBeInstanceOf<ShapedRecipe>()
            PaperTestServer.recipe(recipeKey("test:knife", 1)).shouldBeInstanceOf<FurnaceRecipe>()
            isOnServer(recipeKey("test:knife", 2)) shouldBe false
            manager.anvilRecipes.single().key shouldBe recipeKey("test:knife", 2)
        }

        @Test
        fun `makes the prefab's own item unless the set or definition names a result`() {
            recipeSet("test:knife", "recipes: [ $bedrockRecipe ]")
            recipeSet("test:fork", "{ result: minecraft:emerald, recipes: [ $bedrockRecipe ] }")
            recipeSet(
                "test:spoon",
                """
                result: minecraft:emerald
                recipes:
                  - { type: shaped, items: { B: minecraft:bedrock }, shape: [ B ], result: { type: minecraft:gold_ingot, amount: 2 } }
                """
            )
            val manager = registered()

            manager.getRecipe(recipeKey("test:knife"))!!.result shouldBe prefabItem("test:knife")
            manager.getRecipe(recipeKey("test:fork"))!!.result shouldBe stack(Material.EMERALD)
            manager.getRecipe(recipeKey("test:spoon"))!!.result shouldBe stack(Material.GOLD_INGOT, 2)
        }

        @Test
        fun `skips recipes that fail to load and keeps the rest of the set`() {
            prefabResults["test:ghost"] = null
            recipeSet("test:ghost", "recipes: [ $bedrockRecipe, { type: dye, target: minecraft:leather_helmet } ]")
            recipeSet("test:knife", "recipes: [ { type: shaped, items: { K: minecraft:stick }, shape: [ KM ] }, $bedrockRecipe ]")
            val manager = registered()

            isOnServer(recipeKey("test:ghost", 0)) shouldBe false
            manager.getRecipe(recipeKey("test:ghost", 1)).shouldNotBeNull()
            manager.getRecipe(recipeKey("test:knife", 0)).shouldBeNull()
            manager.getRecipe(recipeKey("test:knife", 1)).shouldNotBeNull()
            warnings shouldHaveSize 2
        }
    }

    @Nested
    inner class RemovingRecipes {
        @Test
        fun `keeps a removed vanilla recipe off across reloads and puts it back once no prefab removes it`() {
            recipeSet("test:knife", "{ removeRecipes: [ minecraft:stick ], recipes: [] }")
            registered().close()
            val manager = registered()
            isOnServer(vanilla("stick")) shouldBe false
            warnings.shouldBeEmpty()

            removeRecipeSets()
            manager.registerRecipes()

            RecipeRegistry.holder(vanilla("stick")).shouldNotBeNull().runtimeAddedRecipe() shouldBe false
        }

        @Test
        fun `puts removed recipes back and resends once recipes are disabled`() {
            recipeSet("test:knife", "{ removeRecipes: [ minecraft:stick ], recipes: [] }")
            manager(isStillEnabled = { false }).apply { registerRecipes() }.close()

            isOnServer(vanilla("stick")) shouldBe true
            PaperTestServer.recipeUpdates shouldBe 2
        }
    }

    @Nested
    inner class Resending {
        @Test
        fun `resends to everyone on the first load and once a definition or the item it resolves changed`() {
            recipeSet("test:knife", "recipes: [ $bedrockRecipe ]")
            registered().close()
            PaperTestServer.recipeUpdates shouldBe 1

            prefabResults["test:knife"] = stack(Material.EMERALD)
            registered().close()
            PaperTestServer.recipeUpdates shouldBe 2

            removeRecipeSets()
            recipeSet("test:knife", "recipes: [ { type: shaped, items: { D: minecraft:dirt }, shape: [ D ] } ]")
            registered()
            PaperTestServer.recipeUpdates shouldBe 3
        }

        @Test
        fun `reregisters and resends after a datapack reload once recipes first loaded`() {
            recipeSet("test:knife", "recipes: [ $bedrockRecipe ]")
            val manager = manager()

            datapackReload(manager)
            isOnServer(recipeKey("test:knife")) shouldBe false

            manager.registerRecipes()
            datapackReload(manager)
            PaperTestServer.recipeUpdates shouldBe 2
        }
    }

    @Test
    fun `lets an item denied in vanilla recipes only into geary recipes naming its prefab`() {
        denyInVanilla("test:cloak")
        recipeSet("test:dyes", "recipes: [ { type: dye, target: test:cloak }, { type: dye, target: minecraft:iron_sword } ]")
        val manager = registered()
        val cloak = prefabItem("test:cloak")

        manager.isAllowedIn(manager.getRecipe(recipeKey("test:dyes", 0)), cloak) shouldBe true
        manager.isAllowedIn(manager.getRecipe(recipeKey("test:dyes", 1)), cloak) shouldBe false
        manager.isAllowedIn(null, cloak) shouldBe false
        manager.isAllowedIn(null, prefabItem("test:knife")) shouldBe true
    }

    @Nested
    inner class Discovery {
        @Test
        fun `discovers recipe book recipes of sets asking for it, which the player has permission for`() {
            recipeSet(
                "test:open",
                """
                discoverRecipes: true
                recipes:
                  - $bedrockRecipe
                  - { type: crafter, items: { B: minecraft:bedrock }, shape: [ B ] }
                  - { type: anvil, input: minecraft:iron_pickaxe, material: minecraft:bedrock }
                """
            )
            recipeSet("test:locked", "{ discoverRecipes: true, permission: mia.locked, recipes: [ $bedrockRecipe ] }")
            recipeSet("test:hidden", "recipes: [ $bedrockRecipe ]")
            val manager = registered()
            val player = TestPlayer()
            val permitted = TestPlayer(permissions = setOf("mia.locked"))

            manager.discover(player.player)
            manager.discover(permitted.player)

            player.discovered shouldContainExactly listOf(recipeKey("test:open"))
            permitted.discovered shouldContainExactlyInAnyOrder listOf(recipeKey("test:open"), recipeKey("test:locked"))
        }

        @Test
        fun `discovers recipes for players online when recipes load and for players joining`() {
            recipeSet("test:knife", "{ discoverRecipes: true, recipes: [ $bedrockRecipe ] }")
            val online = TestPlayer().also { PaperTestServer.onlinePlayers += it.player }
            val manager = registered()
            online.discovered shouldContainExactly listOf(recipeKey("test:knife"))

            val joining = TestPlayer()
            RecipeDiscoveryListener(manager).run { PlayerJoinEvent(joining.player, null as Component?).discoverOnJoin() }
            joining.discovered shouldContainExactly listOf(recipeKey("test:knife"))
        }
    }
}
