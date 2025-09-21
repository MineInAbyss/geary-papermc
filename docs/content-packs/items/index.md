# Creating items

Item prefabs are defined by the `set.item` component with a
[[Serializable Item]]. By far the best way to see all configuration options is to use our JSON schema, as shown in [[File structure]], but we'll go over common use-cases.

### Examples

#### Common options

This item shows off some commonly used options for custom items

```yaml
set.item:
  item:
    type: stone
    itemName: <white>Custom Item
    lore:
      - <red>A fancy lore line
    customModelData: 100
    maxStackSize: 99
```

#### Custom food

We support Minecraft's food component, which can apply custom effects, eating time, and more:

```yaml
set.item:
  item:
    type: sand
    food:
      saturation: 1.8
      nutrition: 3
      usingConvertsTo: minecraft:red_sand
      effects:
        - probability: 0.35
          effect:
            type: minecraft:hunger
            duration: 10s
            amplifier: 2
            isAmbient: true
            hasParticles: true
            hasIcon: true
```

#### Tools

Use Minecraft's tool component to create tools with custom mining speeds:

```yaml
set.item:
  item:
    itemName: Charcoal Sand Sickle
    type: minecraft:wooden_hoe
    customModelData: 7
    durability: 3000
    tool:
      rules:
        - blockTypes:
            - "minecraft:leaves"
          speed: 8.0
          correctForDrops: true
```
