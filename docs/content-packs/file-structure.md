# File structure

Geary supports loading content from its config folder. We write custom mobs, items, or blocks in config files called **prefabs**.  When we spawn a mob or create an item, it is an **instance** of a prefab and inherits all of its components.

## Directory structure

Place Geary config files in `plugins/Geary/prefabs/<namespace>`, where `<namespace>` is unique to your project (ex.
mineinabyss). Files will be loaded by name regardless of folder structure, for example:

```markdown
Geary/
└── prefabs/
    └── mineinabyss/
        └── items/
            ├── drops
            ├── equipment
            ├── food/
            │   ├── cut_meat_cooked.yml
            │   └── cut_meat_raw.yml
            ├── materials
            ├── misc
            └── ...
```

## Writing a prefab

Prefab files contain a list of components. Geary comes with components that mark your prefab as an item mob, or block. Note that other than these specialized components, _there is no difference between any type of prefab_, in fact Geary provides many components that can be used identically across items, mobs, or blocks (ex. the `observe` component in Geary's action system.)

In the next few pages we'll explore how to create each kind of prefab, but first we'll explain the common format:

- Components are defined by their name, ex. this file defines `set.item` and `resourcepack` components:
```yaml title="abyssal_snail_gunk.yml"
set.item:
  item:
    type: minecraft:slime_ball
    itemName: <reset><#785F34>Abyssal Snail Gunk
    customModelData: 1
resourcepack:
  textures: mineinabyss:item/creature_drops/abyssal_snail_gunk
```
- Any components not provided by geary must include a namespace, ex. `blocky:sound`:
```yaml title="charcoal_sand_ore.yml"
set.block:
    blockType: NOTEBLOCK
    blockId: 1
blocky:sound:
    placeSound: minecraft:block.amethyst_block.place
    breakSound: minecraft:block.amethyst_block.break
```

We provide a json schema [here](https://raw.githubusercontent.com/MineInAbyss/plugin-schemas/refs/heads/master/generated/geary.json) for automatic code completion, our `server-config` project shows how to set this up in IntelliJ as well as GitHub's web editor using the YAML plugin.
{.tip}
