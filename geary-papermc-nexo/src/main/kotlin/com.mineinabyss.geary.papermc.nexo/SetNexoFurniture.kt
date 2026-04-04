package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey
import com.nexomc.nexo.mechanics.MechanicsManager
import com.nexomc.nexo.mechanics.furniture.FurnitureFactory
import com.nexomc.nexo.utils.NexoYaml
import com.nexomc.nexo.utils.copyWithParent
import com.nexomc.nexo.utils.section
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

//@JvmInline
@Serializable
@SerialName("nexo:furniture")
data class SetNexoFurniture(
    val id: PrefabKey,
    @SerialName("mechanic")
    private val _mechanic: Map<String, @Contextual Any?>,
) {

    val mechanic by lazy {
        val mechanicSection = NexoYaml.toSection(_mechanic)
        val section = YamlConfiguration().createSection(id.full)
        section.set("Mechanics.furniture", mechanicSection)
        factory.parse(section.section("Mechanics.furniture")!!)
    }

    companion object {
        val factory by lazy { MechanicsManager.factory<FurnitureFactory>()!! }
    }
}
