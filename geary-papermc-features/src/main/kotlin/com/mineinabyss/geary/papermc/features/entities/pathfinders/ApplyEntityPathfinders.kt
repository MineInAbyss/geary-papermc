package com.mineinabyss.geary.papermc.features.entities.pathfinders

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.geary.addons.dsl.AddonScope
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Mob
import org.bukkit.plugin.Plugin

fun AddonScope.addPathfindersSystem(): Observer {
    val plugin = get<Plugin>()

    return observe<OnSet>().exec(query<Pathfinders, BukkitEntity>()) { (pathfinders, bukkit) ->
        val mob = (bukkit as? Mob) ?: return@exec
        plugin.launch {
            delay(1.ticks)
            pathfinders.pathfinders.forEach {
                Bukkit.getMobGoals().addGoal(mob, it.priority, it.toPaperPathfinder(mob))
            }
        }
    }
}

