package com.mineinabyss.geary.papermc.features.items.recipes

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.encodePrefabs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.idofront.messaging.ComponentLogger
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach

/** Runs on real Paper item stacks and a geary engine, without registering anything on the server */
abstract class RecipeTestBase : GearyTest() {
    init {
        PaperTestServer.start()
        // Prefab items are marked through this class's own engine, so each test class registers it again
        TestItemProviders.registerProvider("") { item, prefab ->
            item.type = Material.IRON_SWORD
            item.markPrefab(prefab)
            true
        }
    }

    override fun setupGeary() = geary(TestEngineModule).apply {
        serialization { }
        install(Prefabs)
    }

    private val yaml = Yaml(configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property))

    protected val key = NamespacedKey("test", "recipe")

    protected val warnings = mutableListOf<String>()

    protected val recipeLogger = ComponentLogger(StaticConfig(minSeverity = Severity.Warn, logWriterList = listOf(WarningCollector())), "Test")

    @BeforeEach
    fun clearWarnings() = warnings.clear()

    protected fun decodeRecipe(text: String) = yaml.decodeFromString(RecipeDefinition.serializer(), text)

    protected fun decodeSet(text: String) = yaml.decodeFromString(SetRecipes.serializer(), text.trimIndent())

    protected fun decodeIngredient(text: String) = yaml.decodeFromString(SerializableRecipeIngredient.serializer(), text)

    internal fun context(result: ItemStack = stack(Material.DIAMOND), permission: String? = null, category: String = "MISC") =
        RecipeContext(world, key, result, group = "", category, permission, recipeLogger)

    internal fun load(text: String, result: ItemStack = stack(Material.DIAMOND), permission: String? = null) =
        decodeRecipe(text).load(context(result, permission))

    internal inline fun <reified T : CustomRecipe> loadCustom(text: String, result: ItemStack = stack(Material.DIAMOND), permission: String? = null): T =
        load(text, result, permission).shouldNotBeNull().custom.shouldBeInstanceOf<T>()

    internal fun loadIngredient(text: String, enforceAmount: Boolean = false) = decodeIngredient(text).toIngredient(context(), enforceAmount)

    protected fun denyInVanilla(prefab: String) {
        entity {
            set(DenyInVanillaRecipes())
            set(PrefabKey.of(prefab))
        }
    }

    protected fun stack(material: Material, amount: Int = 1): ItemStack = ItemStack.of(material, amount)

    protected fun prefabItem(prefab: String, amount: Int = 1): ItemStack = stack(Material.IRON_SWORD, amount).apply { markPrefab(prefab) }

    protected fun named(material: Material, name: String): ItemStack = stack(material).named(name)

    protected fun ItemStack.named(name: String): ItemStack = apply { setData(DataComponentTypes.CUSTOM_NAME, Component.text(name)) }

    protected fun ItemStack.name(): Component? = getData(DataComponentTypes.CUSTOM_NAME)

    protected fun damaged(material: Material, damage: Int): ItemStack = stack(material).apply { setData(DataComponentTypes.DAMAGE, damage) }

    protected fun ItemStack.damage(): Int? = getData(DataComponentTypes.DAMAGE)

    protected fun maxDamage(material: Material): Int = stack(material).getData(DataComponentTypes.MAX_DAMAGE)!!

    protected fun enchanted(material: Material, vararg levels: Pair<Enchantment, Int>): ItemStack =
        stack(material).apply { setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(levels.toMap())) }

    protected fun ItemStack.enchantmentLevels(): Map<Enchantment, Int>? = getData(DataComponentTypes.ENCHANTMENTS)?.enchantments()

    protected fun matrix(vararg items: ItemStack?, size: Int = 9): Array<ItemStack?> = Array(size) { items.getOrNull(it) }

    protected fun smallMatrix(vararg items: ItemStack?): Array<ItemStack?> = matrix(*items, size = 4)

    internal fun ingredient(
        material: Material,
        amount: Int = 1,
        consumption: Consumption = Consumption.Consume(),
        preserved: List<DataComponentType> = emptyList(),
    ) = RecipeIngredient(stack(material), amount, consumption, preserved, matcher = { it.type == material })

    internal fun predicateIngredient(display: Material, amount: Int = 1, matcher: (ItemStack) -> Boolean) =
        RecipeIngredient(stack(display), amount, matcher = matcher)

    internal fun shaped(vararg shape: String, items: Map<Char, RecipeIngredient>, result: ItemStack = stack(Material.OAK_SLAB)) =
        ShapedCustomRecipe(key, result, RecipeMatcher.grid(shape.toList(), items::get), permission = null)

    internal fun shapeless(vararg ingredients: RecipeIngredient, result: ItemStack = stack(Material.OAK_SLAB)) =
        ShapelessCustomRecipe(key, result, ingredients.toList(), permission = null)

    private fun ItemStack.markPrefab(prefab: String) = editPersistentDataContainer { it.encodePrefabs(listOf(PrefabKey.of(prefab))) }

    private inner class WarningCollector : LogWriter() {
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            warnings += message
        }
    }
}
