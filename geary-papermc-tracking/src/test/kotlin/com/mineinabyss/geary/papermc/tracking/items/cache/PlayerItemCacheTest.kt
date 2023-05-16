package com.mineinabyss.geary.papermc.tracking.items.cache

import be.seeseemelk.mockbukkit.MockBukkit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.mocks.MockItem
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.EntityEncoded
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.PlayerInstanced
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.helpers.prefabs
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test
import java.util.*

class PlayerItemCacheTest {
    init {
        geary(TestEngineModule, TestEngineModule(reuseIDsAfterRemoval = false)) {
            install(Prefabs)
        }
        MockBukkit.mock()
    }

    val prefabKey1 = PrefabKey.of("test:1")
    val prefabKey2 = PrefabKey.of("test:2")
    val prefab1 = entity { set(prefabKey1) }
    val prefab2 = entity { set(prefabKey2) }

    fun mockItemCache(): PlayerItemCache<MockItem> {
        return PlayerItemCache(
            maxSize = 64,
            cacheConverter = { ItemStack(Material.STONE) },
            deserializeItem = { entity() },
            readItemInfo = { it.info },
        )
    }

    @Test
    fun `should create entities correctly for regular items`() {
        // arrange
        val cache = mockItemCache()
        val inventory = arrayOfNulls<MockItem>(64)
        inventory[0] = MockItem(EntityEncoded(UUID.randomUUID()))
        inventory[1] = MockItem(EntityEncoded(UUID.randomUUID()))

        // act
        cache.updateToMatch(inventory)

        // assert
        cache[0].shouldNotBeNull()
        cache[1].shouldNotBeNull()
        cache[0].shouldNotBe(cache[1])
    }


    @Test
    fun `should create entities correctly for player-instanced items`() {
        // arrange
        val cache = mockItemCache()
        val inventory = arrayOfNulls<MockItem>(64)
        inventory[0] = MockItem(PlayerInstanced(setOf(prefabKey1)))
        inventory[1] = MockItem(PlayerInstanced(setOf(prefabKey1)))
        inventory[6] = MockItem(PlayerInstanced(setOf(prefabKey2)))
        inventory[10] = MockItem(PlayerInstanced(setOf(prefabKey2)))

        // act
        cache.updateToMatch(inventory)

        // assert
        cache[0].shouldNotBeNull()
        cache[0].shouldBe(cache[1])
        cache[0]!!.prefabs.shouldContainExactly(prefab1)

        cache[6].shouldNotBeNull()
        cache[6].shouldNotBe(cache[0])
        cache[6].shouldBe(cache[10])

        cache[6]!!.prefabs.shouldContainExactly(prefab2)
    }

    //TODO in the future this should be cached
    @Test
    fun `should re-create item when moved in inventory`() {
        // arrange
        val cache = mockItemCache()
        val inventory = arrayOfNulls<MockItem>(64)
        inventory[0] = MockItem(EntityEncoded(UUID.randomUUID()))

        // act
        cache.updateToMatch(inventory)
        val firstEntity = cache[0]

        inventory[1] = inventory[0]
        inventory[0] = null
        cache.updateToMatch(inventory)

        // assert
        cache[0].shouldBeNull()
        cache[1].shouldNotBeNull()
        cache[1].shouldNotBe(firstEntity)
    }

    @Test
    fun `should keep player instance until all items of type are removed`() {
        // arrange
        val cache = mockItemCache()
        val inventory = arrayOfNulls<MockItem>(64)
        inventory[0] = MockItem(PlayerInstanced(setOf(prefabKey1)))
        inventory[1] = MockItem(PlayerInstanced(setOf(prefabKey1)))

        // act
        cache.updateToMatch(inventory)
        val playerInstancedEntity = cache[0]
        inventory[1] = null
        inventory[2] = MockItem(PlayerInstanced(setOf(prefabKey1)))
        cache.updateToMatch(inventory)

        // assert
        cache[1].shouldBeNull()
        playerInstancedEntity.shouldNotBeNull()
        cache[0].shouldBe(playerInstancedEntity)
        cache[2].shouldBe(playerInstancedEntity)
    }

    @Test
    fun `should remove entity when removed from cache`() {
        // assert
        val cache = mockItemCache()

        val inventory = arrayOfNulls<MockItem>(64)
        inventory[0] = MockItem(PlayerInstanced(setOf(prefabKey1)))

        // act
        cache.updateToMatch(inventory)
        val entity = cache[0]
        cache.updateToMatch(arrayOfNulls(64))

        // assert
        cache[0].shouldBeNull()
        entity.shouldNotBeNull()
        // TODO entity.isRemoved
        shouldThrowAny { entity.set("") }
    }

//    @Test
//    fun `should create correctly when swapping two loaded items`() {
//        // arrange
//        val cache = mockItemCache()
//        val inventory = Array<MockItem?>(64) { null }
//        val item0 = MockItem(EntityEncoded(UUID.randomUUID()))
//        val item1 = MockItem(EntityEncoded(UUID.randomUUID()))
//        inventory[0] = item0
//        inventory[1] = item1
//
//        // act
//        cache.updateToMatch(inventory)
//        val entity0 = cache[0]
//        val entity1 = cache[1]
//        inventory[0] = item1
//        inventory[1] = item0
//        cache.updateToMatch(inventory)
//
//        // assert
//        cache[0].shouldNotBeNull()
//        cache[1].shouldNotBeNull()
//        cache[0].shouldNotBeIn(entity0, entity1, cache[1])
//        cache[1].shouldNotBeIn(entity0, entity1, cache[0])
//    }
}
