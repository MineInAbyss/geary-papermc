package com.mineinabyss.geary.papermc.bridge.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.game.components.CooldownManager
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.papermc.tracking.items.components.SlotType
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val INTERVAL = 1.seconds

class CooldownDisplaySystem : RepeatingSystem(interval = INTERVAL) {
    private val Pointer.cooldownManager by get<CooldownManager>()
    private val Pointer.held by family { has<SlotType.Held>() }

    @OptIn(UnsafeAccessors::class)
    override fun Pointer.tick() {
        entity.parent?.with { player: Player ->
            player.sendActionBar(Component.text(cooldownManager.incompleteCooldowns.entries.joinToString("\n") { (key, cooldown) ->
                val length = cooldown.length.milliseconds
                val timeLeft = (cooldown.endTime - System.currentTimeMillis()).milliseconds
                val squaresLeft =
                    if (timeLeft < INTERVAL) 0 else (timeLeft / length * displayLength).roundToInt()

                buildString {
                    append("$key ")
                    append(Color.GREEN)
                    repeat(displayLength - squaresLeft) {
                        append(displayChar)
                    }
                    append(ChatColor.RED)
                    repeat(squaresLeft) {
                        append(displayChar)
                    }
                    if (timeLeft < INTERVAL) append(
                        ChatColor.GREEN,
                        " [✔]"
                    ) else append(ChatColor.GRAY, " [$timeLeft]")
                }
            }))
        }
    }

    companion object {
        private const val displayLength = 10
        private const val displayChar = '■'
    }
}
