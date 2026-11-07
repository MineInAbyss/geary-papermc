package com.mineinabyss.geary.papermc.features.items.consumables

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.encodePrefabs
import com.mineinabyss.geary.papermc.features.items.recipes.PaperTestServer
import com.mineinabyss.geary.papermc.features.items.recipes.TestItemProviders
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.idofront.serialization.SerializableItemStack
import io.kotest.matchers.shouldBe
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConsumableMatcherTest : GearyTest() {
    private var providerCalls = 0

    init {
        PaperTestServer.start()
    }

    override fun setupGeary() = geary(TestEngineModule).apply {
        serialization { }
        install(Prefabs)
    }

    @BeforeEach
    fun registerGearyLikeProvider() {
        TestItemProviders.registerProvider("") { item, prefab ->
            providerCalls++
            val key = PrefabKey.of(prefab)
            val setItem = world.entityOfOrNull(key)?.get<SetItem>() ?: return@registerProvider false
            setItem.item.toItemStackOrNull(item) ?: return@registerProvider false
            item.editPersistentDataContainer { with(world) { it.encodePrefabs(listOf(key)) } }
            true
        }
        prefab("test:bandage", SerializableItemStack(type = Material.CLOCK, _itemName = "Bandage"))
        prefab("test:fibers", SerializableItemStack(type = Material.CLOCK, _itemName = "Fibers"))
        providerCalls = 0
    }

    private fun prefab(key: String, item: SerializableItemStack) {
        entity {
            set(PrefabKey.of(key))
            set(SetItem(item))
        }
    }

    private fun built(prefab: String, amount: Int = 1): ItemStack =
        SerializableItemStack(prefab = prefab).toItemStack().apply { this.amount = amount }

    private fun matcher(template: SerializableItemStack) = with(world) { ConsumableMatcher.of(template) }

    private fun assertSameAsMatches(template: SerializableItemStack, items: List<ItemStack>) {
        val matcher = matcher(template)
        items.forEach { matcher.matches(it) shouldBe template.matches(it) }
    }

    private val candidates
        get() = listOf(
            built("test:bandage"),
            built("test:bandage", amount = 16),
            built("test:bandage").apply { setData(DataComponentTypes.CUSTOM_NAME, Component.text("Renamed")) },
            built("test:fibers"),
            ItemStack.of(Material.CLOCK),
            ItemStack.of(Material.COBBLESTONE, 64),
            ItemStack.empty(),
        )

    @Test
    fun `prefab template gives the same result as matches`() {
        assertSameAsMatches(SerializableItemStack(prefab = "test:bandage"), candidates)
    }

    @Test
    fun `template with an explicit type gives the same result as matches`() {
        assertSameAsMatches(SerializableItemStack(type = Material.CLOCK, prefab = "test:bandage"), candidates)
        assertSameAsMatches(SerializableItemStack(type = Material.PAPER, prefab = "test:bandage"), candidates)
        assertSameAsMatches(SerializableItemStack(type = Material.COBBLESTONE), candidates)
    }

    @Test
    fun `missing prefab falls back to matches`() {
        assertSameAsMatches(SerializableItemStack(prefab = "test:missing"), candidates)
    }

    @Test
    fun `only items that can match build the template`() {
        val matcher = matcher(SerializableItemStack(prefab = "test:bandage"))
        val others = listOf(built("test:fibers"), ItemStack.of(Material.CLOCK), ItemStack.of(Material.COBBLESTONE, 64))
        val bandage = built("test:bandage")
        providerCalls = 0

        others.forEach { matcher.matches(it) shouldBe false }
        providerCalls shouldBe 0
        matcher.matches(bandage) shouldBe true
        providerCalls shouldBe 1
    }
}
