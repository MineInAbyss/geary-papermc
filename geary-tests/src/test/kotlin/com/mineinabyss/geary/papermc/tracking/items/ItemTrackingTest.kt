package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.helpers.MockedServerTest
import com.mineinabyss.geary.papermc.helpers.SomeData
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.idofront.serialization.SerializableItemStack
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.Material
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class ItemTrackingTest : MockedServerTest() {
    init {
        geary(TestEngineModule) {
            serialization {
                components {
                    component(SomeData.serializer())
                }
            }
            install(EntityTracking)
            install(ItemTracking)
            install(Prefabs)
        }
        geary.pipeline.runStartupTasks()
    }

    val prefabKey = PrefabKey.of("test:prefab")
    val prefab = entity {
        set(SetItem(SerializableItemStack(type = Material.GLASS)))
        setPersisting(SomeData("test"))
        set(prefabKey)
    }

    @Nested
    inner class ItemProviderTests {
        @Test
        fun `should only encode prefab to PDC`() {
            // act
            val item = itemTracking.createItem(prefabKey)

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
        val player = server.addPlayer()

        // act
        val cache = player.toGeary().get<PlayerItemCache<*>>()

        // assert
        cache.shouldNotBeNull()
    }

    @Test
    fun `should encode components to item when removed`() {
        // arrange
        val player = server.addPlayer()
        val item = itemTracking.createItem(prefabKey).shouldNotBeNull()
        val gearyPlayer = player.toGeary()
        val inventory = player.inventory

        // act
        inventory.addItem(item)
//        gearyPlayer.get<PlayerItemCache<*>>().updateToMatch(inventory)

        // assert
    }
}
