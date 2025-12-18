package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.helpers.*
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.features.FeatureManagerBuilder
import com.mineinabyss.idofront.serialization.SerializableItemStack
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.inventory.ItemStackMock

class ItemTrackingTest : MockedServerTest() {
    override fun GearySetup.setupGeary() {
        serialization {
            withTestSerializers()
        }
        install(Prefabs)
    }

    override fun FeatureManagerBuilder.setupFeatureManager() {
        install(TestItemTracking)
        install(TestEntityTracking)
    }

    val prefabKey = PrefabKey.of("test:prefab")
    val prefab = entity {
        set(SetItem(SerializableItemStack(type = Material.GLASS)))
        setPersisting(SomeData("test"))
        set(prefabKey)
    }

    @Test
    fun `mock test`() {
        val item = ItemStackMock.of(Material.STRING)
        item.type shouldBe Material.STRING
        item.editPersistentDataContainer { }
        item.type shouldBe Material.STRING

    }

    @Nested
    inner class ItemProviderTests {
        @Test
        fun `should only encode prefab to PDC when creating item`() {
            // act
            val item = world.getAddon(ItemTracking).createItem(prefabKey)

            // assert
            item.shouldNotBeNull()
            item.type.shouldBe(Material.GLASS)

            val pdc = item.itemMeta.persistentDataContainer
            pdc.decodePrefabs().shouldContainExactly(prefabKey)
            pdc.decode<SomeData>().shouldBeNull()
        }
    }

    @Test
    fun `should create item cache when player logs in`() {
        // arrange
        val player = createTestPlayer()

        // act
        val cache = player.toGeary().get<PlayerItemCache<*>>()
        val gearyInv = player.inventory.toGeary()

        // assert
        cache.shouldNotBeNull()
        gearyInv.shouldNotBeNull()
    }

    @Test
    fun `should get up-to-date entity with ItemStack component when item added to inventory`() {
        // arrange
        val player = createTestPlayer()
        val item = world.getAddon(ItemTracking).createItem(prefabKey).shouldNotBeNull()
        val inventory = player.inventory
        val gearyInv = inventory.toGeary().shouldNotBeNull()

        // act
        inventory.setItem(10, item)
        val entityInInv = gearyInv.get(10)

        // assert
        entityInInv.shouldNotBeNull()
        entityInInv.get<ItemStack>().shouldBe(inventory.getItem(10))
    }

    @Test
    fun `should be able to get item in main hand`() {
        // arrange
        val player = createTestPlayer()
        val item = world.getAddon(ItemTracking).createItem(prefabKey).shouldNotBeNull()
        player.inventory.setItemInMainHand(item)
        val gearyInv = player.inventory.toGeary().shouldNotBeNull()

        // act
        val itemInMainHand = gearyInv.itemInMainHand
        val getEquipmentSlot = gearyInv.get(EquipmentSlot.HAND)

        // assert
        itemInMainHand.shouldNotBeNull()
        getEquipmentSlot.shouldNotBeNull()
        itemInMainHand.shouldBe(getEquipmentSlot)
    }
}
