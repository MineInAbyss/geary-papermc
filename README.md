<div align="center">

# Geary PaperMC

[![Build](https://github.com/MineInAbyss/geary-papermc/actions/workflows/build.yml/badge.svg)](https://github.com/MineInAbyss/geary-papermc/actions/workflows/build.yml)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-papermc-core/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-papermc-core)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://docs.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://docs.mineinabyss.com)

</div>

geary-papermc is a [Paper](https://papermc.io/) plugin that lets you interact with Minecraft through our Entity Component System, [Geary](https://github.com/MineInAbyss/Geary). 

## Features

- Tracks ECS entities for Bukkit mobs, items, and blocks for you
  - **Currently only mob tracking is complete**
- Easily store and persist data on anything that's tracked
- Write your behaviour using familiar Bukkit listeners, or use Geary systems to make repeating tasks
- Let users create configurable prefabs in yaml, json, and more under `plugins/Geary/<namespace>` folders

## Example

### Prefabs

We have several config-driven projects to help you create custom things without having to code! These are currently being rewritten for our simpler structure and have examples on their corresponding page.

- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom mobs. Includes pathfinding, attributes, and a spawning system
- [Looty](https://github.com/MineInAbyss/Looty) - Custom items, recipes, and migration support as your config changes
- [Blocky](https://github.com/MineInAbyss/Blocky) - Custom blocks, furniture and more


### Mobs

```kotlin
val player: Player = ...
val gearyPlayer = mob.toGeary()

@Serializable
data class Coins(val amount: Int)

player.setPersisting(Coins(10))
```

## Usage

- Install from our [releases](https://github.com/MineInAbyss/geary-papermc/releases/latest)
  Depend on `Geary` in your plugin's `plugin.yml`
- We depend on [Idofront](https://github.com/MineInAbyss/Idofront/releases), install it into your plugins folder
- For full ECS usage, see the [Geary wiki](https://docs.mineinabyss.com/geary/)

### Gradle

```kotlin
repositories {
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    implementation("com.mineinabyss:geary-papermc:x.y.z")
}
```

This will include common addons that come preinstalled for Minecraft (ex prefabs). Use `geary-papermc-core` if you don't want those added to your project.

## Projects using Geary

### From Mine in Abyss

- [Blocky](https://github.com/MineInAbyss/Blocky) - Custom blocks for geary
- [Packy](https://github.com/MineInAbyss/Packy) - Resourcepack generator, merger, and server
- [Chatty](https://github.com/MineInAbyss/Chatty) - Customizes chat messages with MiniMessage support
- [Mine in Abyss](https://github.com/MineInAbyss/MineInAbyss) - Our main plugin with relics and curse
- [Stamina Climb](https://github.com/MineInAbyss/StaminaClimb) - BoTW-like climbing system
- [Bonfire](https://github.com/MineInAbyss/Blocky) - Custom respawning system
- [ExtraCommands](https://github.com/MineInAbyss/ExtraCommands) - Useful utility commands
- [Eternal fortune](https://github.com/MineInAbyss/EternalFortune) - Graves plugin
- [Particles](https://github.com/MineInAbyss/particles) - Display entity particle demo
- [Backpacky](https://github.com/MineInAbyss/Backpacky) - Backpacks
