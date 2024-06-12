package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.key.Key

class PrefabKeyArgument : CustomArgumentType.Converted<PrefabKey, Key> {

    override fun getExamples() = nativeType.examples

    override fun getNativeType() = ArgumentTypes.key()

    override fun convert(nativeType: Key) = PrefabKey.Companion.of(nativeType.asString())
}