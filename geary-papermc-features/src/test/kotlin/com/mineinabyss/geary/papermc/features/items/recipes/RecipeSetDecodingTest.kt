package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializerOrNull
import org.bukkit.Material
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.KClass

class RecipeSetDecodingTest : RecipeTestBase() {
    @Test
    fun `reads a set of recipes with every ingredient form`() {
        val recipes = decodeSet(
            """
            group: knives
            permission: mia.craft.knife
            recipes:
              - type: shaped
                items:
                  K: { item: test:knife, damage: 1 }
                  M: test:raw_meat
                shape: [ "K", "M" ]
              - type: shapeless
                items: [ test:knife, { type: minecraft:stick, amount: 2 } ]
                result: minecraft:stick
              - type: anvil
                input: test:knife
                material: { item: minecraft:iron_ingot, amount: 2 }
                repair: 50
            """
        )
        recipes.group shouldBe "knives"
        recipes.permission shouldBe "mia.craft.knife"

        val shaped = recipes.recipes[0].shouldBeInstanceOf<ShapedRecipeDefinition>()
        shaped.items.getValue("K").damage shouldBe 1
        shaped.items.getValue("M").item.prefab shouldBe "test:raw_meat"
        shaped.result.shouldBeNull()

        val shapeless = recipes.recipes[1].shouldBeInstanceOf<ShapelessRecipeDefinition>()
        shapeless.items[1].amount shouldBe 2
        shapeless.result?.type shouldBe Material.STICK

        val anvil = recipes.recipes[2].shouldBeInstanceOf<AnvilRecipeDefinition>()
        anvil.material.amount shouldBe 2
        anvil.requiresResult shouldBe false
    }

    // Geary's autoscan looks serializers up reflectively, which fails for a class whose companion is private
    @OptIn(InternalSerializationApi::class)
    @Test
    fun `every serializable class exposes its serializer the way geary's autoscan looks it up`() {
        val packageName = RecipeDefinition::class.java.packageName
        val loader = RecipeDefinition::class.java.classLoader
        val scanned: List<KClass<*>> = loader.getResources(packageName.replace('.', '/')).toList()
            .filter { it.protocol == "file" }
            .flatMap { File(it.toURI()).listFiles { file -> file.name.endsWith(".class") }.orEmpty().toList() }
            .map { Class.forName("$packageName.${it.nameWithoutExtension}", false, loader) }
            .filter { it.isAnnotationPresent(Serializable::class.java) }
            .map { it.kotlin }

        scanned.map { it.simpleName } shouldContainAll listOf("AnvilRecipeDefinition", "SetRecipes", "DenyInVanillaRecipes", "SerializableRecipeIngredient")
        scanned.forEach { kClass ->
            withClue(kClass.simpleName) { kClass.serializerOrNull().shouldNotBeNull() }
        }
    }
}
