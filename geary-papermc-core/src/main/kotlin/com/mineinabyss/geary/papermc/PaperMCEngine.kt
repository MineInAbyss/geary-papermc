package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.idofront.time.ticks
import org.bukkit.Bukkit

class PaperMCEngine : ArchetypeEngine(tickDuration = 1.ticks) {
    private val plugin get() = gearyPaper.plugin

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            tick(Bukkit.getServer().currentTick.toLong())
        }, 0, 1)
    }
}
