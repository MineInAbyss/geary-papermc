package com.mineinabyss.geary.papermc.features.general.cooldown

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.features.general.cooldown.CooldownDisplayProps.INTERVAL
import com.mineinabyss.geary.papermc.features.general.cooldown.CooldownDisplayProps.displayChar
import com.mineinabyss.geary.papermc.features.general.cooldown.CooldownDisplayProps.displayLength
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.time.ticks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CooldownsToRemove(
    val cooldowns: List<GearyEntity>
)

fun GearyModule.createCooldownDisplaySystem() = system(object : Query() {
    val player by get<Player>()
    val cooldowns by getRelationsWithData<CooldownStarted, Any?>()
}).every(INTERVAL).defer {
    //TODO this should be a separate system once we have system priorities set up
    val validCooldowns = cooldowns.filter { relation ->
        if (!relation.target.exists()) {
            return@filter false
        }
        val cooldown = relation.data.cooldown
        val timeLeft = cooldown.length - (System.currentTimeMillis() - relation.data.time)
            .toDuration(DurationUnit.MILLISECONDS)
        if (timeLeft.isNegative()) {
            return@filter false
        }
        true
    }
    val invalidCooldowns = cooldowns - validCooldowns.toSet()

    val cooldownsWithDisplay = validCooldowns.mapNotNull { relation ->
        val cooldown = relation.target.get<Cooldown>() ?: return@mapNotNull null
        if (cooldown.displayName == null) return@mapNotNull null

        val timeLeft = cooldown.length - (System.currentTimeMillis() - relation.data.time)
            .toDuration(DurationUnit.MILLISECONDS)
        CooldownInfo(cooldown.displayName, timeLeft, cooldown.length)
    }

    player.sendActionBar(
        Component.join(JoinConfiguration.commas(true), cooldownsWithDisplay.map { cooldown ->
            val squaresLeft =
                if (cooldown.timeLeft < INTERVAL) 0 else (cooldown.timeLeft / cooldown.length * displayLength).roundToInt()

            val cooldownRender = Component.textOfChildren(
                Component.text(displayChar.toString().repeat(displayLength - squaresLeft), NamedTextColor.GREEN),
                Component.text(displayChar.toString().repeat(squaresLeft), NamedTextColor.RED),
                if (cooldown.timeLeft < INTERVAL) Component.text(" [✔]", NamedTextColor.GREEN)
                else Component.text(
                    " [${cooldown.timeLeft.toString(DurationUnit.SECONDS, 2)}]",
                    NamedTextColor.GRAY
                )
            ).compact()

            Component.textOfChildren(cooldown.display, Component.space(), cooldownRender)
        })
    )
    CooldownsToRemove(invalidCooldowns.map { it.target })
}.onFinish { data: CooldownsToRemove, entity ->
    data.cooldowns.forEach { target ->
        entity.removeRelation<CooldownStarted>(target)
    }
}

class CooldownInfo(val display: Component, val timeLeft: Duration, val length: Duration)


object CooldownDisplayProps {
    const val displayLength = 10
    const val displayChar = '■'
    val INTERVAL = 1.ticks
}
