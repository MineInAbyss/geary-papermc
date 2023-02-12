<div align="center">

# Geary PaperMC

[![Build](https://github.com/MineInAbyss/geary-papermc/actions/workflows/build.yml/badge.svg)](https://github.com/MineInAbyss/geary-papermc/actions/workflows/build.yml)
[![Package](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/geary-papermc-core/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/geary-papermc-core)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://wiki.mineinabyss.com/geary)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://wiki.mineinabyss.com/contribute)

</div>

geary-papermc is a [Paper](https://papermc.io/) plugin that lets you interact with Minecraft through our Entity Component System, [Geary](https://github.com/MineInAbyss/Geary). 

## Features

- Tracks ECS entities for Bukkit mobs, items, and (soon to be moved here) blocks for you
- Easily store and persist data on anything that's tracked
- Write your behaviour using familiar Bukkit listeners, or use Geary systems to make repeating tasks super clean
- Let users create configurable prefabs in yaml, json, and more under `plugins/Geary/<namespace>` folders

## Usage

Coming soon after we finish updating our own plugins now that we've separated Geary and Geary-papermc.


## Plugins using Geary

We have several config-driven projects to help you create custom things without having to code!

- [Mobzy](https://github.com/MineInAbyss/Mobzy) - Custom mobs. Includes pathfinding, attributes, and a spawning system
- [Looty](https://github.com/MineInAbyss/Looty) - Custom items, recipes, and migration support as your config changes
- [Blocky](https://github.com/MineInAbyss/Blocky) - Custom blocks, furniture and more

As well as projects that make lighter use of Geary to store data on players.

- [Chatty](https://github.com/MineInAbyss/Chatty) - Customizes chat messages with MiniMessage support
