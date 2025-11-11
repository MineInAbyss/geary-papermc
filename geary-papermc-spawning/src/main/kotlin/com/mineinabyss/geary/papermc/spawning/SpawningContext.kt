package com.mineinabyss.geary.papermc.spawning

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.tasks.SpreadSpawnTask
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Database
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class SpawningContext(
    val reader: SpawnEntryReader,
    val spreadSpawnTask: SpreadSpawnTask,
    val database: Database,
    val plugin: GearyPlugin,
    val prettyPrintJson: Json,
    val logger: Logger,
    val dao: SpawnLocationsDAO,
) {
    val spawns = reader.readSpawnEntries()
    val spawnEntriesByName: Map<String, SpawnEntry> = spawns.mapValues { it.value.entry }

    fun sendTpButton(player: Player, spawnLocation: SpreadSpawnLocation) {
        val loc = spawnLocation.location
        val command = "/tp ${loc.x} ${loc.y} ${loc.z}"
        val distance = if (loc.world != null) loc.distance(player.location).toInt() else -1

        val message = " • ${spawnLocation.stored.type} <gray>(${distance}m away)".miniMsg()
            .hoverEvent(
                """
                |id: <gray>${spawnLocation.id}</gray>
                |location: <gray>[${loc.x.toInt()}, ${loc.y.toInt()}, ${loc.z.toInt()}]</gray>
                |stored: <gray>${prettyPrintJson.encodeToString(spawnLocation.stored)}</gray>
            """.trimMargin().miniMsg()
            )
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
        player.sendMessage(message)
    }

    fun dumpDB(loc: Location, player: Player) {
        val db = database ?: error("No database to dump")
        val dao = SpawnLocationsDAO()
        plugin.launch {
            db.read {
                val locations = dao.getSpawnsNear(loc, 10000.0)
                player.sendMessage("Total spawn locations: ${locations.size}")
                locations.forEach { location ->
                    sendTpButton(player, location)
                }
            }
        }
    }

    // the db is locked when we try to run this function.
    fun clearDB(world: World) {
        plugin.launch {
            database.write {
                dao.dropAll(world)
            }
        }
    }
}