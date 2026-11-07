package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import com.mineinabyss.geary.papermc.spawning.spread_spawn.ChunkRect
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadPos
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import io.kotest.matchers.shouldBe
import org.bukkit.Location
import org.bukkit.util.BoundingBox
import org.junit.jupiter.api.Test

class SpreadSectionTests {
    private val type = object : SpawnType {
        override val key = "mm:test"
        override val category = SpawnCategory("test")
        override fun spawnAt(location: Location) = error("Not spawnable in tests")
    }

    private fun section(verticalWeight: Double = 1.0, sectionMinY: Int = -220, sectionMaxY: Int = 220) = SpreadSpawner.Section.of(
        type = "praying_skeleton",
        regionName = "l1s0",
        config = SpreadSpawnConfig(
            entry = SpawnEntry(type = type), spreadRadius = 10, spawnCap = 2,
            sectionMinY = sectionMinY, sectionMaxY = sectionMaxY, verticalWeight = verticalWeight,
        ),
        enableAltSpawns = false,
        min = SpreadPos(-20, -100, 0),
        max = SpreadPos(31, 50, 15),
    )

    @Test
    fun `section should include the region's max blocks`() {
        val section = section()
        section.box shouldBe BoundingBox(-20.0, -100.0, 0.0, 32.0, 51.0, 16.0)
        section.chunks shouldBe ChunkRect(-2, 0, 1, 0)
        section.contains(SpreadPos(31, 50, 15)) shouldBe true
        section.contains(SpreadPos(32, 50, 15)) shouldBe false
        section.containsXZ(-20, 0) shouldBe true
        section.containsXZ(-21, 0) shouldBe false
    }

    @Test
    fun `section y range should intersect the region and config`() {
        section().let { (it.minY to it.maxY) shouldBe (-100 to 50) }
        section(sectionMinY = -10, sectionMaxY = 20).let { (it.minY to it.maxY) shouldBe (-10 to 20) }
    }

    @Test
    fun `query box should grow by the radius scaled vertically`() {
        section().queryBox(-256, 256) shouldBe BoundingBox(-30.0, -110.0, -10.0, 42.0, 61.0, 26.0)
        section(verticalWeight = 0.5).queryBox(-256, 256) shouldBe BoundingBox(-30.0, -120.0, -10.0, 42.0, 71.0, 26.0)
        section(verticalWeight = 0.0).queryBox(-256, 256) shouldBe BoundingBox(-30.0, -256.0, -10.0, 42.0, 256.0, 26.0)
    }

    @Test
    fun `cap counting should only include rows inside the section`() {
        val rows = listOf(SpreadPos(0, 0, 0), SpreadPos(31, 50, 15), SpreadPos(35, 0, 0), SpreadPos(0, 60, 0))
        rows.count { section().contains(it) } shouldBe 2
    }
}
