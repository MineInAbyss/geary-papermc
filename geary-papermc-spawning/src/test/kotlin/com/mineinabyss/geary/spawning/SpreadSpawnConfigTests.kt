package com.mineinabyss.geary.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.bukkit.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class SpreadSpawnConfigTests {
    private fun type(key: String) = object : SpawnType {
        override val key = key
        override val category = SpawnCategory("test")
        override fun spawnAt(location: Location) = error("Not spawnable in tests")
    }

    private val main = SpawnEntry(type = type("mm:main"))
    private val alt = SpawnEntry(type = type("mm:alt"))

    private fun config(altChance: Double) = SpreadSpawnConfig(
        entry = main, spreadRadius = 10, spawnCap = 1, sectionMinY = 0, sectionMaxY = 10,
        altSpawnChance = altChance, altSpawnEntry = alt,
    )

    @BeforeEach
    fun mockTypes() {
        mockkObject(SpawnType.Companion)
        every { SpawnType.getType(any()) } answers { type(firstArg()) }
    }

    @AfterEach
    fun unmockTypes() = unmockkObject(SpawnType.Companion)

    // Excerpt of the live survival spread_config.yml.
    // Conditions are left out since decoding EnsureAction needs a running Geary world
    private val legacyConfig = """
        spawnDelay: 20
        worldName: "world"
        clearSpawnsOlderThan: 4h
        types:
          praying_skeleton:
            enableAltSpawns: false
            worldName: "world"
            sectionsConfig:
              l1s0:
                entry:
                  type: "mm:prayingSkeleton_l1"
                spreadRadius: 55
                spawnCap: 15
                sectionMinY: -220
                sectionMaxY: -20
                spawnNoise: 5
                splitSize: 55
                spawnAttempts: 1000
                altSpawnChance: 0.0
                altSpawnEntry:
                  type: "mm:goldenSkeleton"
              l4s1:
                entry:
                  type: "mm:prayingSkeleton_l4"
                spreadRadius: 400
                spawnCap: 8
                sectionMinY: -220
                sectionMaxY: 220
                spawnNoise: 5
                splitSize: 100
                spawnAttempts: 500
    """.trimIndent()

    @Test
    fun `old style config should decode with the new defaults`() {
        val decoded = Yaml(configuration = YamlConfiguration(strictMode = false))
            .decodeFromString(SpreadEntityTypesConfig.serializer(), legacyConfig)

        decoded.spawnDelay shouldBe 20
        decoded.clearSpawnsOlderThan shouldBe 4.hours
        decoded.minPlayerDistance shouldBe 24.0
        decoded.surfaceSamplesPerChunk shouldBe 32
        decoded.surfaceRescanAfter shouldBe 3.days

        val sections = decoded.types.getValue("praying_skeleton").sectionsConfig
        sections.keys shouldBe setOf("l1s0", "l4s1")
        val l1 = sections.getValue("l1s0")
        l1.entry.type.key shouldBe "mm:prayingSkeleton_l1"
        l1.altSpawnEntry.type.key shouldBe "mm:goldenSkeleton"
        l1.candidateCount shouldBe 24
        l1.verticalWeight shouldBe 1.0
        sections.getValue("l4s1").altSpawnEntry.type.key shouldBe "mm:prayingSkeleton_l4"
    }

    @Test
    fun `alt entries should only be chosen when enabled with a positive chance`() {
        val random = Random(1)
        repeat(100) {
            config(100.0).chooseEntry(enableAltSpawns = false, random) shouldBe main
            config(0.0).chooseEntry(enableAltSpawns = true, random) shouldBe main
            config(100.0).chooseEntry(enableAltSpawns = true, random) shouldBe alt
        }
    }

    @Test
    fun `entryForType should resolve main and alt entries by key`() {
        val config = config(50.0)
        config.entryForType("mm:main") shouldBe main
        config.entryForType("mm:alt") shouldBe alt
        config.entryForType("mm:unknown").shouldBeNull()
    }
}
