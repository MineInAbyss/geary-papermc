package com.mineinabyss.geary.papermc.spawning.statistics

import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.time.ticks
import io.lumine.mythic.bukkit.MythicBukkit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.time.Duration

class EntityStatistics : Listener {
    val statsThread = CoroutineScope(Dispatchers.IO.limitedParallelism(1))

    data class EntityDataframe(
        val x: Int,
        val y: Int,
        val z: Int,
        val entityType: String,
        val mmType: String?,
        val timeLived: Duration,
        val playerName: String?,
    )

    data class DeathData(
        val x: Int,
        val y: Int,
        val z: Int,
        val mmType: String?,
        val timeLived: Duration,
        val deathTime: Instant,
        val damageCause: String?,
    )

    private val deaths = CacheBuilder.newBuilder().maximumSize(5000L).build<DeathData, Unit>()

    @EventHandler
    fun EntityDeathEvent.logDeaths() {
        val mm = MythicBukkit.inst().mobManager
        val mythicMob = if (mm.isMythicMob(entity)) mm.getMythicMobInstance(entity) else null
        val deathData = DeathData(
            x = entity.location.blockX,
            y = entity.location.blockY,
            z = entity.location.blockZ,
            mmType = mythicMob?.mobType,
            timeLived = entity.ticksLived.ticks,
            deathTime = Instant.now(),
            damageCause = entity.lastDamageCause?.cause?.name,
        )
        deaths.put(deathData, Unit)
    }

    fun getActiveEntitiesDataframe(): DataFrame<EntityDataframe> {
        val mm = MythicBukkit.inst().mobManager
        return Bukkit.getWorlds()
            .asSequence()
            .flatMap { it.entities }
            .map {
                val bukkit = it
                val geary = it.toGearyOrNull()
                val mythicMob = if (mm.isMythicMob(it)) mm.getMythicMobInstance(it) else null

                EntityDataframe(
                    x = bukkit.location.blockX,
                    y = bukkit.location.blockY,
                    z = bukkit.location.blockZ,
                    entityType = bukkit.type.name,
                    mmType = mythicMob?.mobType,
                    timeLived = bukkit.ticksLived.ticks,
                    playerName = (bukkit as? Player)?.name,
                )
            }
            .toList()
            .toDataFrame()
    }


    fun dumpData() = statsThread.launch {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")
        val now = LocalDateTime.now().format(formatter)
        val dest = (gearyPaper.plugin.dataPath / "statistics" / now).createDirectories().toFile()
        Geary.i { "Dumping entity information..." }
        getActiveEntitiesDataframe().writeCSV(dest.resolve("entities.csv").also { it.createNewFile() })
        deaths.asMap().keys.toDataFrame().writeCSV(dest.resolve("deaths.csv").also { it.createNewFile() })
        Geary.i { "Dumped entity information to $dest" }
    }

    fun reset() {
        deaths.invalidateAll()
    }
}
