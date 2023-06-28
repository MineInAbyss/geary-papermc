package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.helpers.MockedServerTest
import com.mineinabyss.geary.papermc.helpers.withMockTracking
import com.mineinabyss.geary.papermc.tracking.items.components.PlayerInstancedItem
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.SerializableItemStack
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.Material
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.streams.asStream

class SetItemMigrationTest : MockedServerTest() {
    init {
        geary(TestEngineModule) {
            withMockTracking()
        }
    }

    fun itemPrefabs() = sequence<PrefabKey> {
        val prefabKey1 = PrefabKey.of("test:prefab1")
        entity {
            set(SetItem(SerializableItemStack(type = Material.GLASS)))
            set(prefabKey1)
        }
        yield(prefabKey1)

        val prefabKey2 = PrefabKey.of("test:prefab2")
        entity {
            set(SetItem(SerializableItemStack(type = Material.GLASS)))
            add<PlayerInstancedItem>()
            set(prefabKey2)
        }
        yield(prefabKey2)
    }.asStream()

    @ParameterizedTest
    @MethodSource("itemPrefabs")
    fun `should respect SetItem migration when tracking an item created before the migration`(
        prefab: PrefabKey
    ) {
        // arrange
        val player = server.addPlayer()
        val inventory = player.inventory
        val gearyInventory = inventory.toGeary().shouldNotBeNull()
        val item = gearyItems.createItem(prefab)

        // act
        val typeBefore = item?.type
        prefab.toEntity().set(SetItem(SerializableItemStack(type = Material.STONE)))
        inventory.setItem(1, item)
        gearyInventory.forceRefresh()
        val typeAfter = inventory.getItem(1)?.type

        // assert
        typeBefore.shouldBe(Material.GLASS)
        typeAfter.shouldBe(Material.STONE)
    }

    fun `should not migrate item properties when marked as overridden`() {

    }
}
