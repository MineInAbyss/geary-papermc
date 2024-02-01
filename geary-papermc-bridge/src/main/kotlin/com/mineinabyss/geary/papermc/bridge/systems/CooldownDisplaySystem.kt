package com.mineinabyss.geary.papermc.bridge.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.papermc.bridge.conditions.Cooldown
import com.mineinabyss.geary.papermc.bridge.conditions.CooldownStarted
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.idofront.time.ticks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val INTERVAL = 1.ticks

@AutoScan
class CooldownDisplaySystem : RepeatingSystem(interval = INTERVAL) {
    val Pointer.player by get<Player>()
    val Pointer.cooldowns by getRelationsWithData<CooldownStarted, Any?>()

    class CooldownInfo(val display: Component, val timeLeft: Duration, val length: Duration)

    @OptIn(UnsafeAccessors::class)
    override fun Pointer.tick() {
        //TODO this should be a separate system once we have system priorities set up
        val validCooldowns = cooldowns.filter { relation ->
            fun removeCooldown() = entity.removeRelation<CooldownStarted>(relation.target)
            if (!relation.target.exists()) {
                removeCooldown()
                return@filter false
            }
            val cooldown = relation.data.cooldown
            val timeLeft = cooldown.length - (System.currentTimeMillis() - relation.data.time)
                .toDuration(DurationUnit.MILLISECONDS)
            if (timeLeft.isNegative()) {
                removeCooldown()
                return@filter false
            }
            true
        }

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
    }

    companion object {
        private const val displayLength = 10
        private const val displayChar = '■'
    }
}
