package com.mineinabyss.geary.papermc.services

import org.bukkit.inventory.ItemStack

interface GearyItemService {
    /**
     * @return a geary item by prefab [namespace] and [key] or null if not found.
     */
    fun getItem(namespace: String, key: String): ItemStack?

    /**
     * @return a geary item by [namespaceKey], containing namespace and key separated by `:`, ex. `mineinabyss:custom_apple`, or null if not found.
     */
    fun getItem(namespaceKey: String): ItemStack?
}