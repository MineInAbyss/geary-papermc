package com.mineinabyss.geary.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.idofront.config.ConfigEntryWithKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class SpawnLocationConfigTests {
    private val world = mockk<World>(relaxed = true)

    private fun box(min: Double, max: Double, override: Boolean = false) = SpawnLocationConfig(
        locMin = Location(world, min, min, min),
        locMax = Location(world, max, max, max),
        gearySpawnOverride = override,
    )

    private fun unified(vararg regions: Pair<String, SpawnLocationConfig>) = SpawnLocationsUnified(
        listOf(ConfigEntryWithKey(Path("locations.yml"), "locations", SpawnLocationsConfig(mapOf(*regions))))
    )

    @BeforeEach
    fun mockWorlds() {
        mockkStatic(Bukkit::class)
        every { Bukkit.getWorlds() } returns listOf(world)
        every { Bukkit.getWorld(any<String>()) } returns null
    }

    @AfterEach
    fun unmockWorlds() = unmockkStatic(Bukkit::class)

    @Test
    fun `box regions should test each axis against its own range`() {
        val region = SpawnLocationConfig(
            locMin = Location(world, 0.0, 50.0, 0.0),
            locMax = Location(world, 10.0, 60.0, 10.0),
        )

        region.isInside(5.0, 55.0, 5.0) shouldBe true
        region.isInside(55.0, 55.0, 5.0) shouldBe false
        region.isInside(5.0, 5.0, 5.0) shouldBe false
        region.isInside(5.0, 55.0, 11.0) shouldBe false
    }

    @Test
    fun `overrideAt should pick the smallest matching override after non matching entries`() {
        val regions = unified(
            "plain" to box(-100.0, 100.0),
            "elsewhere" to box(500.0, 600.0, override = true),
            "large" to box(-50.0, 50.0, override = true),
            "small" to box(-5.0, 5.0, override = true),
        )

        regions.overrideAt(Location(world, 0.0, 0.0, 0.0))?.key shouldBe "small"
        regions.overrideAt(Location(world, 20.0, 20.0, 20.0))?.key shouldBe "large"
        regions.overrideAt(Location(world, 90.0, 90.0, 90.0)).shouldBeNull()
    }

    @Test
    fun `locations files should decode from the Locations key`() {
        val yaml = """
            Locations:
              cave:
                locMin: 0,10,0
                locMax: 16,20,16
                gearySpawnOverride: true
        """.trimIndent()

        val decoded = Yaml(configuration = YamlConfiguration(strictMode = false))
            .decodeFromString(SpawnLocationsConfig.serializer(), yaml)

        val cave = decoded.locations.getValue("cave")
        cave.gearySpawnOverride shouldBe true
        cave.isInside(8.0, 15.0, 8.0) shouldBe true
    }
}
