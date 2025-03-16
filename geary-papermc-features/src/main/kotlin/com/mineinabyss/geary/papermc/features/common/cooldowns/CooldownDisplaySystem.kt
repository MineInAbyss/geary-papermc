package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.features.common.cooldowns.CooldownDisplayProps.INTERVAL
import com.mineinabyss.geary.papermc.features.common.cooldowns.CooldownDisplayProps.displayChar
import com.mineinabyss.geary.papermc.features.common.cooldowns.CooldownDisplayProps.displayLength
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

fun Geary.cooldownDisplaySystem() = system(query<Player, Cooldowns>())
    .every(INTERVAL)
    .exec { (player, cooldowns) ->
        if (!cooldowns.hasDisplayableCooldowns) return@exec

        val cooldownsWithDisplay = cooldowns.cooldowns.values.filter {
            it.display != null && it.isVisible()
        }

        if (cooldownsWithDisplay.isEmpty()) return@exec

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

                Component.textOfChildren(cooldown.display!!, Component.space(), cooldownRender)
            })
        )
    }

object CooldownDisplayProps {
    const val displayLength = 10
    const val displayChar = '■'
    val INTERVAL = 1.ticks
    val CLEAR_OLD_COOLDOWNS_INTERVAL = 1.seconds
}
