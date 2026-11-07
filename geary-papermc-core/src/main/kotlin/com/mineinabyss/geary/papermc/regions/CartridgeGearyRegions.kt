package com.mineinabyss.geary.papermc.regions

import com.mineinabyss.cartridge.region.RegionFlag
import com.mineinabyss.cartridge.region.RegionSetting
import com.mineinabyss.cartridge.region.Regions
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/** Only loaded after checking that the Cartridge API is on the classpath. */
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
