package com.mineinabyss.geary.papermc.scripting

import org.bukkit.Location

object Patterns {
    // Runs in a spiral pattern at location, facing dir, with a radius of radius, and a length of length, and *count* points
    inline fun spiral(
        start: Location,
        end: Location,
        radius: Double = 1.0,
        count: Int,
        startOffset: Double = 0.0,
        endOffset: Double = 0.0,
        run: PatternPoint.() -> Unit,
    ) {
    }

    data class PatternPoint(
        val location: Location,
        val direction: Location,
        val distance: Double,
    )
}
