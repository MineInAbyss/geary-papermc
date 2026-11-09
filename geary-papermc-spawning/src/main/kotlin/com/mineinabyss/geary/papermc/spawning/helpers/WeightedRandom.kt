package com.mineinabyss.geary.papermc.spawning.helpers

import java.util.concurrent.ThreadLocalRandom

/** Picks an element with probability proportional to its [weight], or null when no element has a positive weight */
inline fun <T> List<T>.weightedRandomOrNull(weight: (T) -> Double): T? {
    var sum = 0.0
    for (element in this) sum += weight(element).coerceAtLeast(0.0)
    if (sum <= 0.0) return null

    var roll = ThreadLocalRandom.current().nextDouble(sum)
    for (element in this) {
        roll -= weight(element).coerceAtLeast(0.0)
        if (roll < 0.0) return element
    }
    return lastOrNull { weight(it) > 0.0 }
}
