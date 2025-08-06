package com.mineinabyss.geary.papermc.spawning.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import me.dvyy.sqlite.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.Plugin

class TargetEntityDeathListener(
    private val spawner: SpreadSpawner,
    val db: Database,
    val plugin: Plugin
) :
    Listener {

    fun sendTpButton(player: Player, loc: Location) {
        val command = "/tp ${loc.x} ${loc.y} ${loc.z}"
        val message = Component.text("TP to (${loc.x}, ${loc.y}, ${loc.z})")
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
        player.sendMessage(message)
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        dumpDB(db, event.entity.location, event.entity.killer)
        val entity = event.entity.toGearyOrNull() ?: return println("EntityDeathEvent: Entity is not a Geary entity.")
        val spread = entity.get<SpreadSpawnLocation>() ?: return println("no spread component")
        val pos = event.entity.location
        val id = spread.id

        val dao: SpawnLocationsDAO = spawner.dao
        println("removed spawn location $id")
        println("db loc is ${spread.location.x}, ${spread.location.y}, ${spread.location.z}")
        plugin.launch {
            db.write {
                dao.deleteSpawnLocation(spawner.world, id)
            }
        }
    }
    fun dumpDB(db: Database, loc: Location, player : Player?) {
        if (player == null)
            return println("no player to dump db to")
        plugin.launch {
            db.read {
                val locations = spawner.dao.getSpawnsNear(loc, 10000.0)
                player.sendMessage("Total spawn locations: ${locations.size}")
                locations.forEach { location ->
                    sendTpButton(player, location.location)
                }
                println("Total spawn locations: ${locations.size}")
            }
        }
    }
}