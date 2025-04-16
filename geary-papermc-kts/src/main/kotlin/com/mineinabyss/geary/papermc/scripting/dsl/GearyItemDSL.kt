package com.mineinabyss.geary.papermc.scripting.dsl

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.scripting.builders.ItemBuilder
import com.mineinabyss.geary.papermc.scripting.PassiveDSL


class GearyItemDSL(val entity: GearyEntity): Geary by entity.world {
    private val item: ItemBuilder = ItemBuilder()
    private val passiveBuilder: PassiveDSL = PassiveDSL(entity)

    fun item(block: ItemBuilder.() -> Unit) {
        item.apply(block)
    }

    fun passive(run: PassiveDSL.() -> Unit) {
        passiveBuilder.apply(run)
    }

    inline fun components(vararg component: Any) {

    }
}

fun customItem(block: GearyItemDSL.() -> Unit): GearyItemBuilder {
    return GearyItemBuilder(block)
}

fun <T> customItem(block: GearyItemDSL.(T) -> Unit): GearyItemDSL {
    TODO()
//    return GearyItemDSL().apply(block)
}
