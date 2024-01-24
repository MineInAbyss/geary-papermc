package com.mineinabyss.geary.papermc.tracking.entities

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.helpers.MockedServerTest
import com.mineinabyss.geary.papermc.helpers.SomeData
import com.mineinabyss.geary.papermc.helpers.TestEntityTracking
import com.mineinabyss.geary.papermc.helpers.withTestSerializers
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.uuid.UUIDTracking
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

class EntityTrackingTests: MockedServerTest() {
    init {
        geary(TestEngineModule) {
            serialization {
                withTestSerializers()
            }
            install(TestEntityTracking)
            install(UUIDTracking)
        }
        geary.pipeline.runStartupTasks()
    }

    @Test
    fun `should track player on login`() {
        // act
        val player = server.addPlayer()

        // assert
        val gearyPlayer = player.toGearyOrNull().shouldNotBeNull()
        gearyPlayer.get<Player>().shouldBe(player)
        gearyPlayer.get<BukkitEntity>().shouldBe(player)
    }

    @Test
    fun `should untrack player on logout`() {
        // act
        val player = server.addPlayer()
        val entityWhenConnected = player.toGearyOrNull()

        player.disconnect()
        val entityWhenDisconnected = player.toGearyOrNull()

        // assert
        entityWhenConnected.shouldNotBeNull()
        entityWhenDisconnected.shouldBeNull()
    }

    @Test
    fun `should track on mob spawn`() {
        // arrange
        val pig = world.spawn(world.spawnLocation, Pig::class.java)

        // act
        EntityAddToWorldEvent(pig, world).callEvent() // Not called by MockBukkit

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
            removeEntity: () -> Unit
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
            val player = server.addPlayer()

            testPersistence(
                player.toGeary(),
                player.persistentDataContainer,
                player::disconnect
            )
        }

        @Test
        fun `should persist components on entities when chunk unloaded`() {
            val pig = world.spawn(world.spawnLocation, Pig::class.java)
            EntityAddToWorldEvent(pig, world).callEvent()

            testPersistence(
                pig.toGeary(),
                pig.persistentDataContainer
            ) {
                EntitiesUnloadEvent(pig.location.chunk, listOf(pig)).callEvent()
            }
        }
    }
}
