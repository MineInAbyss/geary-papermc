package com.mineinabyss.geary.papermc.bridge

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.actions.components.PotionEffects
import com.mineinabyss.geary.papermc.bridge.components.Touched
import com.mineinabyss.geary.papermc.commons.events.configurable.components.ApplyBuilder
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventTriggers
import com.mineinabyss.geary.papermc.configlang.ConfigLang
import com.mineinabyss.geary.papermc.helpers.MockedServerTest
import com.mineinabyss.geary.papermc.helpers.SomeData
import com.mineinabyss.geary.papermc.helpers.TestEntityTracking
import com.mineinabyss.geary.papermc.helpers.withTestSerializers
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.BukkitBackedItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.serialization.dsl.serialization
import io.kotest.inspectors.forSingle
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.bukkit.entity.Zombie
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.junit.jupiter.api.Test

class EventTriggersTests : MockedServerTest() {
    val prefabKey = PrefabKey.of("geary:test")

    init {
        geary(TestEngineModule) {
            install(TestEntityTracking)
            install(ItemTracking, BukkitBackedItemTracking())
            install(ConfigLang)
            install(PaperBridge)
            install(Prefabs)
            serialization {
                withTestSerializers()
                components {
                    component(PotionEffects::class)
                    component(Touched::class)
                }
            }
        }
        geary.pipeline.runStartupTasks()
    }

    @Test
    fun `should add children via config`() {
        val entity = entity {
            set(
                ChildrenOnPrefab(
                    mapOf(
                        "poison" to listOf(
                            SomeData("testing"),
                            ApplyBuilder("geary:potion_effects")
                        )
                    )
                )
            )
        }
        entity.children.forSingle {
            it.get<SomeData>().shouldBe(SomeData("testing"))
        }
    }

    @Test
    fun `should respect event triggers with child action`() {
        val player = server.addPlayer()
        val mob = world.spawn(world.spawnLocation, Zombie::class.java)
        EntityAddToWorldEvent(mob).callEvent()
        mob.toGeary().apply {
            set(prefabKey)
            set(
                ChildrenOnPrefab(
                    mapOf(
                        "poison" to listOf(
                            PotionEffects(listOf(PotionEffect(PotionEffectType.POISON, 10, 1, true))),
                            ApplyBuilder("geary:potion_effects")
                        )
                    )
                )
            )
            set(EventTriggers(listOf("geary:event.touched this -> this child(poison) other")))
        }
        mob.damage(1.0, player)
        player.activePotionEffects.shouldHaveSize(1)
        val effect = player.activePotionEffects.single()
        effect.type shouldBe PotionEffectType.POISON
        effect.duration shouldBe 10
    }
}
