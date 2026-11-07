package com.mineinabyss.geary.papermc.regions

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

enum class RegionProviderType {
    AUTO, CARTRIDGE, WORLDGUARD
}

sealed interface RegionCuboid {
    /** Inclusive block corners */
    data class Found(val minX: Int, val minY: Int, val minZ: Int, val maxX: Int, val maxY: Int, val maxZ: Int) : RegionCuboid
    data object Missing : RegionCuboid
    data object NotCuboid : RegionCuboid
}

interface GearyRegions {
    val name: String

    /**
     * Ids of the regions at the location including their parents, lowest priority first.
     * Regions below the highest priority region with a spawn override are left out.
     */
    fun spawnRegionsAt(location: Location): List<String>

    fun cuboid(world: World, name: String): RegionCuboid

    fun canBuild(player: Player, location: Location): Boolean

    companion object {
        private val worldGuardInstalled get() = Bukkit.getPluginManager().getPlugin("WorldGuard") != null

        private fun cartridgeOrNull(): GearyRegions? = runCatching {
            Class.forName("com.mineinabyss.cartridge.region.Regions")
            CartridgeGearyRegions()
        }.getOrNull()

        /** AUTO picks Cartridge when it has regions for a loaded world and WorldGuard is not installed, otherwise WorldGuard when installed, otherwise Cartridge */
        fun select(type: RegionProviderType): GearyRegions? = when (type) {
            RegionProviderType.CARTRIDGE -> cartridgeOrNull()
            RegionProviderType.WORLDGUARD -> if (worldGuardInstalled) WorldGuardGearyRegions() else null
            RegionProviderType.AUTO -> {
                val cartridge = cartridgeOrNull()
                when {
                    cartridge is CartridgeGearyRegions && !worldGuardInstalled && cartridge.hasRegions() -> cartridge
                    worldGuardInstalled -> WorldGuardGearyRegions()
                    else -> cartridge
                }
            }
        }

        private var cached: Pair<RegionProviderType, GearyRegions?>? = null

        /** Shared provider for code without access to the DI context, selected once per configured type */
        fun forConfig(type: RegionProviderType): GearyRegions? {
            cached?.let { (cachedType, regions) -> if (cachedType == type) return regions }
            return select(type).also { cached = type to it }
        }
    }
}
