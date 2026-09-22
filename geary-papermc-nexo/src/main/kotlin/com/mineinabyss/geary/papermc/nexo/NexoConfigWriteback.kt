package com.mineinabyss.geary.papermc.nexo

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import org.bukkit.configuration.ConfigurationSection
import java.io.File

/**
 * Nexo picks a custom_variation off the model when a config leaves it out, and saves it back into the item's
 * file so the number stays put. External items have no such file, and their section is rebuilt from the
 * component on every load, so an unpinned variation is reassigned in registration order on the next boot.
 * Placed custom blocks carry it in their vanilla blockdata, so a shift turns them into a different block.
 *
 * Nexo hands us the parsed section instead of saving it, and it gets written into the prefab that declared it.
 */
context(world: WorldScoped)
internal fun persistNexoAssignments(
    prefabKey: PrefabKey,
    mechanic: String,
    section: ConfigurationSection,
    logger: Logger,
) {
    // Only what lives under the mechanic maps onto the component body.
    // Pack.custom_model_data sits outside it and has nowhere to be written back to
    if (section.isInt("Pack.custom_model_data")) logger.w(
        "Nexo assigned a custom_model_data to $prefabKey that cannot be pinned to its prefab, " +
            "give it an itemModel so Nexo stops generating one"
    )

    val mechanicSection = section.getConfigurationSection("Mechanics.$mechanic") ?: return
    val file = prefabKey.toEntityOrNull()?.get<Prefab>()?.file?.let { File(it.toString()) }
        ?: return logger.w("Cannot pin the variations of $prefabKey, it was not loaded from a file")

    // A directional parent without explicit children gets one variation per child, pinned for the same reason
    val assigned = buildList {
        mechanicSection.takeIf { it.isInt("custom_variation") }?.let { add(listOf("custom_variation") to it.getInt("custom_variation")) }
        mechanicSection.getConfigurationSection("directional")?.let { directional ->
            directional.getKeys(false).filter { it.endsWith("_variation") && directional.isInt(it) }
                .forEach { add(listOf("directional", it) to directional.getInt(it)) }
        }
    }

    assigned.forEach { (path, variation) ->
        runCatching { file.insertComponentKey("nexo:$mechanic", path, variation) }
            .onSuccess { if (it) logger.i("Pinned ${path.joinToString(".")} $variation onto $prefabKey") }
            .onFailure { logger.w("Failed pinning ${path.joinToString(".")} $variation onto $prefabKey: ${it.message}") }
    }
}

/**
 * Inserts the key at the end of [path] into this prefab's top-level [component] block, walking any
 * nested blocks before it, and returns whether anything was written.
 *
 * Edited as text rather than reserialized, prefabs carry comments worth keeping and geary cannot write one
 * back anyway, its prefab serializer only decodes
 */
private fun File.insertComponentKey(component: String, path: List<String>, value: Int): Boolean {
    val lines = readLines()
    var header = lines.indexOfFirst { it.trimEnd() == "$component:" }
    if (header == -1) return false
    var body = blockBody(lines, header)

    // Each nested block must already be there, a block that is missing was never part of the config
    for (block in path.dropLast(1)) {
        header = body.firstOrNull { lines[it].trim() == "$block:" } ?: return false
        body = blockBody(lines, header)
    }
    val key = path.last()
    if (body.any { lines[it].trimStart().startsWith("$key:") }) return false

    val indent = body.firstOrNull { lines[it].isNotBlank() }
        ?.let { lines[it].takeWhile(Char::isWhitespace) }
        ?: lines[header].takeWhile(Char::isWhitespace) + "  "

    val updated = lines.subList(0, header + 1) + "$indent$key: $value" + lines.subList(header + 1, lines.size)
    writeText(updated.joinToString("\n", postfix = "\n"))
    return true
}

/** The lines under [header] indented deeper than it, a block ends at the next line that is not */
private fun blockBody(lines: List<String>, header: Int): IntRange {
    val indent = lines[header].takeWhile(Char::isWhitespace).length
    val end = (header + 1..lines.lastIndex)
        .firstOrNull { lines[it].isNotBlank() && lines[it].takeWhile(Char::isWhitespace).length <= indent } ?: lines.size
    return header + 1 until end
}
