package com.mineinabyss.geary.papermc

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource


/**
 * Launches an async task that repeats as close to [period] as possible (in time).
 *
 * Ex. when [period] is 1 second and [block] takes less than 1 second, it will be called every second.
 * If [block] takes longer than 1 second, it will be immediately re-executed.
 *
 * Unlike [launchTickRepeating], if the server begins to lag, this task will continue to be called at the same
 * *time* interval, not the tick rate.
 *
 * This function is recommended for tasks that do not interact much with the main Minecraft thread.
 */
inline fun Plugin.launchTimedRepeating(
    period: Duration,
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend CoroutineScope.() -> Unit,
) = launch(asyncDispatcher + context) {
    while (true) {
        val start = TimeSource.Monotonic.markNow()
        block()
        val elapsed = start.elapsedNow()
        delay((period - elapsed).coerceAtLeast(0.milliseconds))
    }
}

/***
 * Launches an async task that repeats every [periodTicks], or longer if the task takes too long.
 *
 * Ex. if [periodTicks] is 20 ticks and [block] takes less than 20 ticks, it will be called every 20th tick.
 * If [block] takes longer than 20 ticks, it will be immediately re-executed.
 *
 * Unlike [launchTimedRepeating], if the server begins to lag, this task will run less often, since it respects the
 * *tick* rate, not real time.
 *
 * This function is recommended for tasks that interact with the main Minecraft thread, or may directly contribute to server lag.
 */
inline fun Plugin.launchTickRepeating(
    periodTicks: Long,
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend CoroutineScope.() -> Unit,
) = launch(asyncDispatcher + context) {
    val channel = Channel<Unit>(Channel.CONFLATED)
    val repeatingTask = Bukkit.getScheduler().runTaskTimer(this@launchTickRepeating, Runnable {
        channel.trySend(Unit)
    }, 0, periodTicks)

    try {
        channel.consumeEach {
            block()
        }
    } finally {
        repeatingTask.cancel()
    }
}
