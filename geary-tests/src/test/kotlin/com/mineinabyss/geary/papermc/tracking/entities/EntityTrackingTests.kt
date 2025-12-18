package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.helpers.MockedServerTest
import com.mineinabyss.geary.papermc.helpers.SomeData
import com.mineinabyss.geary.papermc.helpers.TestEntityTracking
import com.mineinabyss.geary.papermc.helpers.withTestSerializers
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.features.FeatureManagerBuilder
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.persistence.PersistentDataContainer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EntityTrackingTests : MockedServerTest() {
    override fun GearySetup.setupGeary() {
        serialization {
            withTestSerializers()
        }
        install(UUIDTracking)
    }

    override fun FeatureManagerBuilder.setupFeatureManager() {
        install(TestEntityTracking)
    }

    @Test
    fun `should track player on login`() {
        // arrange & act
        val player = createTestPlayer()

        // assert
        val gearyPlayer = player.toGearyOrNull().shouldNotBeNull()
        gearyPlayer.get<Player>().shouldBe(player)
        gearyPlayer.get<BukkitEntity>().shouldBe(player)
    }

    //
    @Test
    fun `should untrack player on logout`() {
        // arrange & act
        val player = createTestPlayer()
        val entityWhenConnected = player.toGearyOrNull()

        player.disconnect()
        val entityWhenDisconnected = player.toGearyOrNull()

        // assert
        entityWhenConnected.shouldNotBeNull()
        entityWhenDisconnected.shouldBeNull()
    }

    @Test
    fun `should track on mob spawn`() {
        // arrange & act
        val pig = spawnTestEntity()

        // assert
        val gearyPig = pig.toGearyOrNull().shouldNotBeNull()
        gearyPig.get<BukkitEntity>().shouldBe(pig)
        gearyPig.get<Pig>().shouldBe(pig)
    }

    @Nested
    inner class PersistenceTests {
        private fun testPersistence(
            entity: GearyEntity,
            pdc: PersistentDataContainer,
            removeEntity: () -> Unit,
        ) {
            // act
            entity.setPersisting(SomeData("test"))
            val dataBeforeDisconnect = pdc.decode<SomeData>()
            removeEntity()
            val dataAfterDisconnect = pdc.decode<SomeData>()

            // assert
            dataBeforeDisconnect.shouldBeNull()
            dataAfterDisconnect.shouldBe(SomeData("test"))
        }

        @Test
        fun `should persist components when player disconnects`() {
            val player = createTestPlayer()

            testPersistence(
                player.toGeary(),
                player.persistentDataContainer,
                player::disconnect
            )
        }

        @Test
        fun `should persist components on entities when chunk unloaded`() {
            val pig = spawnTestEntity()

            testPersistence(
                pig.toGeary(),
                pig.persistentDataContainer
            ) {
                EntitiesUnloadEvent(pig.location.chunk, listOf(pig)).callEvent()
            }
        }
    }
}
