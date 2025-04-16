package com.mineinabyss.geary.papermc.scripting

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.scripting.dsl.GearyItemDSL
import com.mineinabyss.geary.papermc.tracking.entities.toBukkit
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.query
import org.bukkit.entity.LivingEntity
import kotlin.time.Duration

class PassiveDSL(val entity: GearyEntity) {
    fun match(query: Query, every: Duration, run: suspend ItemContext.() -> Unit) = with(entity.world) {
        val systemMatchingId = entity().id
        entity.add(systemMatchingId)
        system(query { add(query.buildFamily()); has(systemMatchingId) }).every(every).execOnAll {
            entities().fastForEach { entity ->
                runCatching {
                    val holder = entity.parent?.toBukkit<LivingEntity>() ?: return@runCatching
                    val context = ItemContext(
                        entity,
                        itemHolder = holder,
                        coroutineContext = gearyPaper.plugin.minecraftDispatcher
                    )
                    gearyPaper.plugin.launch {
                        run(context)
                    }
                }.onFailure { it.printStackTrace() }
            }
        }
    }
}
