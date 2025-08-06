package com.mineinabyss.geary.papermc.spawning.targeted

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import org.bukkit.Bukkit.getWorlds
import org.bukkit.Location

// NOTE: the names/description of this file and everything associated with it is subject to change
//       most of the names/descriptions are placeholder / semi placeholder

// TODO: Change the names/descriptions to be more fitting

/**
 * Targeted Spawning: a system used to spawn entities in a non-regular way. Ie: in a targeted way.
 * A target could be one of many things; but for now its main purpose will be to allow us to spawn entities in a spread out way.
 *
 * The flow of a targeted spawn is as follows:
 *  - Determine a target in the world
 *  - Determine if the target is valid (ie: is there any skelies nearby ?)
 *  - Add the target to a list of targets
 *  - When a chunk containing a target is loaded: spawn it
 *
 *  The code should then execute the following steps: (for skelies)
 *  - Get the list of skelies of a section
 *  - Calculate where the closest is
 *  - If it is far away enough, attempt to spawn one
 *  - If the spawn was successful (ie: a valid location was found), add it to the list of skelies
 *
 *  The logic should trigger on chunk load (or whenever geary usually tries to run the spawn checks)
 **/
class TargetedSpawner(
    val minRadius: Int = 500,
    val sectionXRange: IntRange = 80519..83321,
    val sectionZRange: IntRange = -1401..1397,
    val splitSize: Int = 250,
    val noiseFactor: Double = 500.0,
    val noiseRange: Int = 10,
    val chunkYRange: IntRange = 0..220,
    val openAreaSize: Int = 2, // for isOpenArea: checks -openAreaSize..openAreaSize
    val openAreaHeight: IntRange = 0..4
) {
    val dao = SpawnLocationsDAO()
    val world = getWorlds().firstOrNull() ?: throw IllegalStateException("No world found")
}
