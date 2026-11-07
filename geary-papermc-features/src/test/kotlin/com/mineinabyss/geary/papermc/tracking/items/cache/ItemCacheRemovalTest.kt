package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnEntityRemoved
import com.mineinabyss.geary.papermc.features.items.recipes.PaperTestServer
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ItemCacheRemovalTest : GearyTest() {
    init {
        PaperTestServer.start()
    }

    data class CachedItem(val slot: Int)

    inner class TestCache(holder: GearyEntity) : PlayerItemCache<ItemStack>(holder) {
        override fun readItemInfo(item: ItemStack) = ItemInfo.EntityEncoded
        override fun convertToItemStack(item: ItemStack) = item
        override fun deserializeItem(item: ItemStack): GearyEntity = entity { set(CachedItem(item.amount)) }
        override fun skipUpdate(slot: Int, newItem: ItemStack?) = getCachedItem(slot) === newItem
        override fun skipReserialization(slot: Int, newItem: ItemStack?) = false
    }

    private val removed = mutableListOf<Int>()
    private lateinit var bystanders: List<GearyEntity>
    private lateinit var holder: GearyEntity
    private lateinit var cache: TestCache
    private lateinit var items: List<GearyEntity>
    private lateinit var rootBefore: Set<ULong>

    @BeforeEach
    fun setup() {
        resetEngine()
        removed.clear()
        clearItemCachesOnRemoval()
        observe<OnEntityRemoved>().exec(query<CachedItem>()) { (item) -> removed += item.slot }
        bystanders = List(10) { entity() }
        holder = entity()
        cache = TestCache(holder)
        holder.set<PlayerItemCache<*>>(cache)
        val inventory = arrayOfNulls<ItemStack>(PlayerItemCache.MAX_SIZE)
        for (slot in 1..3) inventory[slot] = ItemStack(Material.STONE, slot)
        cache.updateToMatch(inventory)
        items = (1..3).map { cache[it]!! }
        rootBefore = rootArchetype.entities.toSet()
    }

    private fun assertBystandersIntact() {
        rootArchetype.entities.toSet() shouldContainAll rootBefore
        bystanders.forEach { it.exists() shouldBe true }
    }

    @Test
    fun `removing the holder removes each cached item once`() {
        holder.removeEntity()

        removed shouldContainExactlyInAnyOrder listOf(1, 2, 3)
        items.forEach { it.exists() shouldBe false }
        assertBystandersIntact()
    }

    @Test
    fun `removing the cache component clears it`() {
        holder.remove<PlayerItemCache<*>>()

        removed shouldContainExactlyInAnyOrder listOf(1, 2, 3)
        holder.exists() shouldBe true
        assertBystandersIntact()
    }

    @Test
    fun `clearing skips items that were already removed`() {
        items[0].removeEntity()
        cache.clear()

        removed shouldContainExactlyInAnyOrder listOf(1, 2, 3)
        cache[1].shouldBeNull()
        assertBystandersIntact()
    }
}
