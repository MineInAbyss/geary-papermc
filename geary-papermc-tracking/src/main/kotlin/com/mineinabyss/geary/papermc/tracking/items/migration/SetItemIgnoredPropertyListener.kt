package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.idofront.nms.nbt.fastPDC
import com.mineinabyss.idofront.serialization.SerializableItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent

class SetItemIgnoredPropertyListener : Listener {
    @EventHandler
    fun PrepareAnvilEvent.addNameOverrideOnRename() {
        if (inventory.firstItem?.fastPDC?.hasComponentsEncoded != true) return

        result?.editMeta {
            val existingIgnore = it.persistentDataContainer.decode<SetItemIgnoredProperties>()?.ignore ?: setOf()
            it.persistentDataContainer.encode(SetItemIgnoredProperties(existingIgnore.plus(SerializableItemStack.Properties.DISPLAY_NAME)))
        }
    }
}
