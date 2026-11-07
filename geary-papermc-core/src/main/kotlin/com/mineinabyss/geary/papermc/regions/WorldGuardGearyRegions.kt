package com.mineinabyss.geary.papermc.regions

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.BooleanFlag
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/** Only loaded when WorldGuard is installed. */
class WorldGuardGearyRegions : GearyRegions {
    private val regionContainer = WorldGuard.getInstance().platform.regionContainer
    override val name = "WorldGuard"

    override fun spawnRegionsAt(location: Location): List<String> {
        val allRegions = regionContainer
            .createQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location))
            .regions
            .sortedBy { it.priority }
        val dropAt = allRegions
            .indexOfLast { it.getFlag(OVERRIDE_LOWER_PRIORITY_SPAWNS) == true }
            .coerceAtLeast(0)
        return allRegions.drop(dropAt).map { it.id }
    }

    override fun cuboid(world: World, name: String): RegionCuboid {
        val region = regionContainer.get(BukkitAdapter.adapt(world))?.getRegion(name) ?: return RegionCuboid.Missing
        val cuboid = region as? ProtectedCuboidRegion ?: return RegionCuboid.NotCuboid
        val min = cuboid.minimumPoint
        val max = cuboid.maximumPoint
        return RegionCuboid.Found(min.x(), min.y(), min.z(), max.x(), max.y(), max.z())
    }

    override fun canBuild(player: Player, location: Location): Boolean = regionContainer.createQuery()
        .testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD)

    companion object {
        val OVERRIDE_LOWER_PRIORITY_SPAWNS = BooleanFlag("geary-spawn-override")
    }
}
