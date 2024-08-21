package com.mineinabyss.geary.papermc.spawning.helpers

import com.mineinabyss.idofront.util.DoubleRange
import java.util.concurrent.ThreadLocalRandom

/**
 * @see WeightedDice
 */
class WeightedList<T>(val probabilities: Map<T, Double>) {

    private val sum = probabilities.values.sum()

    private val rangedDistribution: Map<T, DoubleRange> = probabilities.let { prob ->
        var binStart = 0.0

        prob.asSequence().sortedBy { it.value }
            .map { it.key to binStart..(it.value + binStart) }
            .onEach { binStart = it.second.endInclusive }
            .toMap()
    }

    /**
     * Randomly selects a `T` value with probability
     */
    fun roll() = ThreadLocalRandom.current().nextDouble(0.0, sum).let {
        rangedDistribution.asIterable().first { rng -> it in rng.value }.key
    }
}
