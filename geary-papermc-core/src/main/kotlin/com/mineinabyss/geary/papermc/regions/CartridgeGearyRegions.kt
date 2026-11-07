package com.mineinabyss.geary.papermc.regions

import com.mineinabyss.cartridge.region.Region
import com.mineinabyss.cartridge.region.RegionFlag
import com.mineinabyss.cartridge.region.RegionSetting
import com.mineinabyss.cartridge.region.Regions
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/** Only loaded after checking that the Cartridge API is on the classpath */
class CartridgeGearyRegions : GearyRegions {
    private val regions: Regions = Regions.regions()
    override val name = "Cartridge"

    fun hasRegions(): Boolean = Bukkit.getWorlds().any { regions.regions(it.name).isNotEmpty() }

    override fun spawnRegionsAt(location: Location): List<String> {
        val applicable = regions.regionsAt(location, true).filter { it.name() != GLOBAL }
        val overrideAt = applicable.indexOfFirst { it.ownSetting(RegionSetting.SPAWN_OVERRIDE) == true }
        val kept = if (overrideAt < 0) applicable else applicable.subList(0, overrideAt + 1)
        return kept.asReversed().map { it.name() }
    }

    // Ignores spawn overrides, which only drop regions, and follows parents like regionsAt with includeParents
    override fun spawnRegionsOverlapping(world: World, minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int): Set<String> {
        val result = HashSet<String>()
        for (region in boxes(world)) {
            if (region.minX > maxX || region.maxX < minX || region.minY > maxY || region.maxY < minY || region.minZ > maxZ || region.maxZ < minZ) continue
            var current: Region? = region.region
            while (current != null) {
                if (current.name() != GLOBAL) result.add(current.name())
                current = current.parent()
            }
        }
        return result
    }

    private class RegionBox(val region: Region, val minX: Int, val minY: Int, val minZ: Int, val maxX: Int, val maxY: Int, val maxZ: Int)

    private class WorldBoxes(val source: List<Region>, val boxes: List<RegionBox>)

    private val worldBoxes = HashMap<String, WorldBoxes>()

    // Cartridge hands out the same list until its regions reload, and bounds() allocates on every call
    private fun boxes(world: World): List<RegionBox> {
        val source = regions.regions(world.name)
        worldBoxes[world.name]?.takeIf { it.source === source }?.let { return it.boxes }
        val boxes = source.mapNotNull { region ->
            val box = region.bounds() ?: return@mapNotNull null
            RegionBox(region, box.minX.toInt(), box.minY.toInt(), box.minZ.toInt(), box.maxX.toInt() - 1, box.maxY.toInt() - 1, box.maxZ.toInt() - 1)
        }
        worldBoxes[world.name] = WorldBoxes(source, boxes)
        return boxes
    }

    override fun cuboid(world: World, name: String): RegionCuboid {
        val region = regions.region(world.name, name) ?: return RegionCuboid.Missing
        val box = region.bounds() ?: return RegionCuboid.NotCuboid
        return RegionCuboid.Found(
            box.minX.toInt(), box.minY.toInt(), box.minZ.toInt(),
            box.maxX.toInt() - 1, box.maxY.toInt() - 1, box.maxZ.toInt() - 1,
        )
    }

    override fun canBuild(player: Player, location: Location): Boolean =
        regions.isAllowed(player, location, RegionFlag.BLOCK_PLACE)

    private companion object {
        const val GLOBAL = "global"
    }
}
