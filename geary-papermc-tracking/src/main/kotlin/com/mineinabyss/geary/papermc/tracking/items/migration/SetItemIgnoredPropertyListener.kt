package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.idofront.nms.nbt.fastPDC
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class SetItemIgnoredPropertyListener : Listener {
    @EventHandler
    fun PrepareAnvilEvent.addNameOverrideOnRename() {
        if (inventory.firstItem?.fastPDC?.hasComponentsEncoded != true) return

        val resultMeta = result?.itemMeta ?: return

        if(resultMeta.hasDisplayName()) {
            // If has a display name set, stop display name migrations
            val existingIgnore = resultMeta.persistentDataContainer.decode<SetItemIgnoredProperties>()?.ignore ?: setOf()
            resultMeta.persistentDataContainer.encode(SetItemIgnoredProperties(existingIgnore.plus(
                BaseSerializableItemStack.Properties.DISPLAY_NAME)))
        } else {
            // If no display name, make sure we allow migrations again
            val existingIgnore = resultMeta.persistentDataContainer.decode<SetItemIgnoredProperties>()?.ignore ?: return
            val ignore = existingIgnore.minus(BaseSerializableItemStack.Properties.DISPLAY_NAME)
            if(ignore.isEmpty()) resultMeta.persistentDataContainer.remove<SetItemIgnoredProperties>()
            else resultMeta.persistentDataContainer.encode(SetItemIgnoredProperties(ignore))
        }
        result?.setItemMeta(resultMeta)
    }
}
