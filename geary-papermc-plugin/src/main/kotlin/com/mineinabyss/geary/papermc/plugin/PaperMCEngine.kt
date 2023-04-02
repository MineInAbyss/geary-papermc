package com.mineinabyss.geary.papermc.plugin

import co.aikar.timings.Timings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.idofront.time.ticks
import org.bukkit.Bukkit

class PaperMCEngine : ArchetypeEngine(tickDuration = 1.ticks) {
    private val plugin get() = gearyPaper.plugin

    override suspend fun RepeatingSystem.runSystem() {
        // Adds a line in timings report showing which systems take up more time.
        val timing = Timings.ofStart(plugin, javaClass.name)
        runCatching {
            doTick()
        }.apply {
            // We want to stop the timing no matter what, but still propagate error up
            timing.stopTiming()
        }.getOrThrow()
    }

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            plugin.launch {
                tick(Bukkit.getServer().currentTick.toLong())
            }
        }, 0, 1)
    }
}
