package com.mineinabyss.geary.papermc.bridge.systems

import kotlin.time.Duration.Companion.seconds

private val INTERVAL = 1.seconds

//class CooldownDisplaySystem : RepeatingSystem(interval = INTERVAL) {
//    private val Pointer.cooldownManager by get<CooldownManager>()
//    private val Pointer.held by family { has<SlotType.Held>() }
//
//    @OptIn(UnsafeAccessors::class)
//    override fun Pointer.tick() {
//        entity.parent?.with { player: Player ->
//            player.sendActionBar(Component.text(cooldownManager.incompleteCooldowns.entries.joinToString("\n") { (key, cooldown) ->
//                val length = cooldown.length.milliseconds
//                val timeLeft = (cooldown.endTime - System.currentTimeMillis()).milliseconds
//                val squaresLeft =
//                    if (timeLeft < INTERVAL) 0 else (timeLeft / length * displayLength).roundToInt()
//
//                buildString {
//                    append("$key ")
//                    append(Color.GREEN)
//                    repeat(displayLength - squaresLeft) {
//                        append(displayChar)
//                    }
//                    append(ChatColor.RED)
//                    repeat(squaresLeft) {
//                        append(displayChar)
//                    }
//                    if (timeLeft < INTERVAL) append(
//                        ChatColor.GREEN,
//                        " [✔]"
//                    ) else append(ChatColor.GRAY, " [$timeLeft]")
//                }
//            }))
//        }
//    }
//
//    companion object {
//        private const val displayLength = 10
//        private const val displayChar = '■'
//    }
//}
