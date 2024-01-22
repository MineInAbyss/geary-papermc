package com.mineinabyss.geary.papermc.bridge.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.papermc.bridge.conditions.Cooldown
import com.mineinabyss.geary.papermc.bridge.conditions.CooldownStarted
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Color
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val INTERVAL = 1.seconds

@AutoScan
class CooldownDisplaySystem : RepeatingSystem(interval = INTERVAL) {
    val Pointer.player by get<Player>()
    val Pointer.cooldowns by getRelationsWithData<CooldownStarted, Any?>()

    class CooldownInfo(val display: Component, val timeLeft: Duration, val length: Duration)

    @OptIn(UnsafeAccessors::class)
    override fun Pointer.tick() {
//        val mainHand = player.inventory.toGeary()?.itemInMainHand ?: return
        val cooldowns = cooldowns.mapNotNull { relation ->
            val cooldown = relation.target.get<Cooldown>() ?: return@mapNotNull null
            val timeLeft = cooldown.length - (System.currentTimeMillis() - relation.data.time)
                .toDuration(DurationUnit.MILLISECONDS)
            if (timeLeft.isNegative()) {
                //TODO separate system should be in charge of this
                entity.removeRelation<CooldownStarted>(relation.target)
                return@mapNotNull null
            }
            CooldownInfo(cooldown.displayName, timeLeft, cooldown.length)
        }
        player.sendActionBar(
            Component.join(JoinConfiguration.newlines(), cooldowns.map { cooldown ->
                val squaresLeft =
                    if (cooldown.timeLeft < INTERVAL) 0 else (cooldown.timeLeft / cooldown.length * displayLength).roundToInt()

                val cooldownRender = buildString {
                    append(" ")
                    append(Color.GREEN)
                    repeat(displayLength - squaresLeft) {
                        append(displayChar)
                    }
                    append(Color.RED)
                    repeat(squaresLeft) {
                        append(displayChar)
                    }
                    if (cooldown.timeLeft < INTERVAL) append(
                        Color.GREEN,
                        " [✔]"
                    ) else append(Color.GRAY, " [${cooldown.timeLeft}]")
                }
                cooldown.display.append(Component.text(cooldownRender))
            })
        )
    }

    companion object {
        private const val displayLength = 10
        private const val displayChar = '■'
    }
}
