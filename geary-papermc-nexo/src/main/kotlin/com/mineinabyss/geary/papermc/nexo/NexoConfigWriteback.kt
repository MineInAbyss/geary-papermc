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

    val variation = section.getConfigurationSection("Mechanics.$mechanic")
        ?.takeIf { it.isInt("custom_variation") }
        ?.getInt("custom_variation") ?: return

    val file = prefabKey.toEntityOrNull()?.get<Prefab>()?.file?.let { File(it.toString()) }
        ?: return logger.w("Cannot pin custom_variation $variation, $prefabKey was not loaded from a file")

    runCatching { file.insertComponentKey("nexo:$mechanic", "custom_variation", variation) }
        .onSuccess { if (it) logger.i("Pinned custom_variation $variation onto $prefabKey") }
        .onFailure { logger.w("Failed pinning custom_variation $variation onto $prefabKey: ${it.message}") }
}

/**
 * Inserts [key] into this prefab's top-level [component] block, returning whether anything was written.
 *
 * Edited as text rather than reserialized, prefabs carry comments worth keeping and geary cannot write one
 * back anyway, its prefab serializer only decodes
 */
private fun File.insertComponentKey(component: String, key: String, value: Int): Boolean {
    val lines = readLines()
    val header = lines.indexOfFirst { it.trimEnd() == "$component:" }
    if (header == -1) return false

    // A top-level component ends at the next line starting in column 0
    val end = (header + 1..lines.lastIndex)
        .firstOrNull { lines[it].isNotBlank() && !lines[it].first().isWhitespace() } ?: lines.size
    val body = header + 1 until end
    if (body.any { lines[it].trimStart().startsWith("$key:") }) return false

    val indent = body.firstOrNull { lines[it].isNotBlank() }
        ?.let { lines[it].takeWhile(Char::isWhitespace) } ?: "  "

    val updated = lines.subList(0, header + 1) + "$indent$key: $value" + lines.subList(header + 1, lines.size)
    writeText(updated.joinToString("\n", postfix = "\n"))
    return true
}
