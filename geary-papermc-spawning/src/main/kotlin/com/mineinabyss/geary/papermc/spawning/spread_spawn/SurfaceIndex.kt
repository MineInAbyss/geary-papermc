package com.mineinabyss.geary.papermc.spawning.spread_spawn

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class SurfaceIndex(
    private val plugin: Plugin,
    private val world: World,
    private val repository: SpreadSpawnRepository,
    private val configs: SpreadEntityTypesConfig,
    private val logger: Logger,
) : Listener {
    private val surfaces = ConcurrentHashMap<Long, ChunkSurface>()
    private val dirty = ConcurrentHashMap.newKeySet<Long>()
    private val materialFlags = MaterialFlags()
    private val minHeight = world.minHeight
    private val maxHeight = world.maxHeight
    private val scanDispatcher = Dispatchers.Default.limitedParallelism(1)

    // Only touched from scanDispatcher, which runs one scan at a time
    private var prefixBuffer: IntArray? = null

    private val savedReader = SavedChunkReader(world, materialFlags)

    private val queue = LinkedHashSet<Long>()
    private val scanning = ConcurrentHashMap.newKeySet<Long>()
    private val savedInFlight = AtomicInteger()

    // Main thread only
    private var savedCursor: Iterator<Long>? = null
    private var nextSavedPass = TimeSource.Monotonic.markNow()
    private var savedBudget = 0.0

    @Volatile
    private var areas: List<ChunkRect> = emptyList()

    @Volatile
    private var areasChanged = false

    @Volatile
    private var loaded = false

    private val writeLock = Mutex()

    val job: Job = SupervisorJob().also { job ->
        plugin.launch(Dispatchers.IO + job) {
            runCatching { repository.loadSurfaces(world) { key, surface -> surfaces.putIfAbsent(key, surface) } }
                .onSuccess { logger.i { "Loaded spread spawn surface index for $it chunks" } }
                .onFailure { logger.e { "Failed to load the surface index, rescanning from scratch: ${it.stackTraceToString()}" } }
            loaded = true
            areasChanged = true
        }
        plugin.launch(Dispatchers.IO + job) {
            try {
                while (true) {
                    delay(1.seconds)
                    runCatching { flushWrites() }.onFailure { logger.e { "Failed to save the surface index: ${it.stackTraceToString()}" } }
                }
            } finally {
                withContext(NonCancellable) { runCatching { flushWrites() } }
            }
        }
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            runCatching { pump() }.onFailure { logger.e { "Surface index pump failed: ${it.stackTraceToString()}" } }
        }, 1, 1)
        job.invokeOnCompletion { task.cancel() }
    }

    /** Safe to call from any thread */
    fun updateAreas(rects: List<ChunkRect>) {
        if (rects == areas) return
        areas = rects
        areasChanged = true
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (event.world != world) return
        enqueue(event.chunk.x, event.chunk.z)
    }

    fun sample(
        rect: ChunkRect,
        minY: Int,
        maxY: Int,
        minOpenness: Int,
        excluded: (x: Int, y: Int, z: Int) -> Boolean,
        count: Int,
        random: Random = Random,
    ): List<SpreadPos> = SpreadSampling.drawCandidates(rect, surfaces::get, minHeight, minY, maxY, minOpenness, excluded, count, random)

    fun stats(rect: ChunkRect, minY: Int, maxY: Int, minOpenness: Int): SurfaceStats =
        SpreadSampling.surfaceStats(rect, surfaces::get, minHeight, minY..maxY, minOpenness)

    fun reject(pos: SpreadPos) {
        val key = pos.chunkKey
        surfaces.computeIfPresent(key) { _, surface ->
            surface.withoutPosition(pos.x and 15, pos.z and 15, pos.y - minHeight)
        } ?: return
        dirty += key
    }

    /** Main thread only */
    fun clear() {
        surfaces.clear()
        dirty.clear()
        queue.clear()
        plugin.launch(Dispatchers.IO + job) { writeLock.withLock { repository.dropSurfaces(world) } }
        areasChanged = true
    }

    private suspend fun flushWrites() = writeLock.withLock {
        if (dirty.isEmpty()) return@withLock
        val batch = HashMap<Long, ChunkSurface>()
        for (key in dirty) if (dirty.remove(key)) surfaces[key]?.let { batch[key] = it }
        if (batch.isEmpty()) return@withLock
        try {
            repository.putSurfaces(world, batch)
        } catch (e: Exception) {
            dirty += batch.keys
            throw e
        }
    }

    private fun needsScan(key: Long): Boolean =
        surfaces[key]?.needsScan(Clock.System.now().epochSeconds, configs.surfaceRescanAfter) ?: true

    private fun enqueue(cx: Int, cz: Int) {
        if (areas.none { it.contains(cx, cz) }) return
        val key = chunkKey(cx, cz)
        if (loaded && !needsScan(key)) return
        queue += key
    }

    private fun pump() {
        if (!loaded) return
        if (areasChanged) {
            areasChanged = false
            savedCursor = null
            world.loadedChunks.forEach { enqueue(it.x, it.z) }
        }
        pumpLoaded()
        pumpSaved()
    }

    private fun pumpLoaded() = repeat(queue.size) {
        if (scanning.size - savedInFlight.get() >= MAX_IN_FLIGHT || queue.isEmpty()) return
        val key = queue.first()
        queue.remove(key)
        if (key in scanning) {
            queue += key
            return@repeat
        }
        val cx = chunkKeyX(key)
        val cz = chunkKeyZ(key)
        if (!world.isChunkLoaded(cx, cz) || !needsScan(key)) return@repeat

        val snapshot = world.getChunkAt(cx, cz).getChunkSnapshot(false, false, false, false)
        launchScan(key, cx, cz) {
            ChunkGrid.build(minHeight, maxHeight, snapshot::isSectionEmpty) { x, y, z -> materialFlags.flagsAt(snapshot, x, y, z) }
        }
    }

    // Walks every chunk in the areas, so spots are drawn from whole sections rather than only where players have been
    private fun pumpSaved() {
        val perSecond = configs.savedChunkScansPerSecond
        if (perSecond == 0) return
        savedBudget = minOf(savedBudget + perSecond / 20.0, MAX_SAVED_IN_FLIGHT.toDouble())

        var steps = 0
        while (savedBudget >= 1 && savedInFlight.get() < MAX_SAVED_IN_FLIGHT && steps++ < MAX_CURSOR_STEPS) {
            val cursor = savedCursor ?: if (nextSavedPass.hasPassedNow()) SpreadSampling.scatteredChunks(areas).also { savedCursor = it } else return
            if (!cursor.hasNext()) {
                savedCursor = null
                nextSavedPass = TimeSource.Monotonic.markNow() + SAVED_PASS_INTERVAL
                return
            }
            val key = cursor.next()
            val cx = chunkKeyX(key)
            val cz = chunkKeyZ(key)
            if (key in scanning || world.isChunkLoaded(cx, cz) || !needsScan(key)) continue

            savedBudget--
            savedInFlight.incrementAndGet()
            launchScan(key, cx, cz, onDone = { savedInFlight.decrementAndGet() }) { savedReader.read(cx, cz) }
        }
    }

    /** [readGrid] runs on [scanDispatcher] and returns null when there is nothing to index */
    private fun launchScan(key: Long, cx: Int, cz: Int, onDone: () -> Unit = {}, readGrid: suspend () -> ChunkGrid?) {
        scanning += key
        plugin.launch(scanDispatcher + job) {
            try {
                val grid = readGrid() ?: return@launch
                val prefix = prefixBuffer ?: IntArray(SurfaceScanner.prefixSize(grid)).also { prefixBuffer = it }
                surfaces[key] = SurfaceScanner.scan(grid, configs.surfaceSamplesPerChunk, Random, Clock.System.now().epochSeconds, prefix)
                dirty += key
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e { "Failed to scan chunk $cx, $cz for the surface index: ${e.stackTraceToString()}" }
            } finally {
                scanning -= key
                onDone()
            }
        }
    }

    private companion object {
        const val MAX_IN_FLIGHT = 4
        // Each region read waits around 250ms in Moonrise's IO queue, so this many in flight sustains about 30 per second
        const val MAX_SAVED_IN_FLIGHT = 8

        // Indexed chunks are skipped without a read, this bounds how many the main thread steps over per tick
        const val MAX_CURSOR_STEPS = 4096
        val SAVED_PASS_INTERVAL = 10.minutes
    }
}
