package com.mineinabyss.geary.papermc.scripting.builders

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

class ItemBuilder {
    var type: Material = Material.STONE
    var hideTooltip: Boolean = false

    fun type(key: Material) {
//        type = Material.getMaterial(key) ?: Material.STONE
    }

    fun name(name: String) {
    }

    fun lore(vararg lore: String) {

    }

    fun customModelData(int: Int) {

    }

    fun enchantments(vararg enchantments: Pair<Enchantment, Int>) {
    }

    fun itemFlags(vararg itemFlags: ItemFlag) {

    }
}
