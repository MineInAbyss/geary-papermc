# Recipes

Geary provides an extensive recipe system for custom or vanilla items.

## Create recipes

Create any kind of recipe for an item, all types listed below.

If the recipe is defined in the item prefab itself, `result` does not need to be specified, the item will be the result.

```yaml
set.recipes:
  recipes: # (1)!
    - type: shaped
      ...
  discoverRecipes: true # (2)!
  group: "" # (3)!
  removeRecipes: [] # (4)!
  result: ... # (5)!
```


1. List of recipes, see available types below.
2. Default `false`, Whether to unlock the recipe automatically when a player joins the server
3. Default empty, set the group of this recipe. Recipes with the same group may be grouped together when displayed in the client
4. Optional, a list of existing recipes to remove (ex. if migrating from one recipe to a new one)
5. A [[Serializable Item]], or don't pass to use this prefab's `set.item`

### Set potionmixes

```yaml
set.potionMixes:
  potionmixes: # (1)!
    - input: ... 
      ingredient: { type: sugar }
  result: ... # (2)!
```

1. List of potion mixes, as defined below
2. A [[Serializable Item]], or don't pass to use this prefab's `set.item`

## Recipe types

### Shaped

Uses keys to define [[Serializable Item]]s as ingredients. These keys can be used to describe a configuration as below

```yaml
- type: shaped
  items:
    M: { type: gold_ingot }
    S: { type: stick }
  configuration: |-
    | MM|
    | SM|
    |S  |
```

A smaller size can also be used for the configuration, ex:

```yaml
configuration: |-
  |MM|
  |SM|
```

### Shapeless

Takes a list of [[Serializable Item]]s as ingredients, they may be placed in any shape.

```yaml
- type: shapeless
  items:
    - type: gold_ingot
    - prefab: myplugin:custom_item
```

### Cooking 

There are several options for cooking recipes, 
`furance`, `blasting`, `campfire`, `smoker`. The input is a [[Serializable Item]] that will be consumed.

```yaml
- type: furnace
  input: { prefab: mineinabyss:ashimite_meat_raw }
  experience: 1f
  cookingTime: 100
```

### Stonecutting

Input is a [[Serializable Item]] that will be consumed by the stonecutter.

```yaml
- type: stonecutting
  input: { type: stone }
```

### Smithing

#### Transform
All inputs are [[Serializable Item]]. template, input, addition are ordered left to right in the smithing table. Ex. we have a template for a custom netherite upgrade:

```yaml
- type: smithing_transform
  template: { type: netherite_upgrade_smithing_template }
  input: { prefab: mineinabyss:diamond_sickle }
  addition: { type: netherite_ingot }
```

#### Trim

### Potionmix

All entries are [[Serializable Item]]s, the input is the potion being brewed, and ingredient is the ingredient being added on top. If input is not specified, will use a water bottle.

```yaml
- type: potionmix
  input: ...
  ingredient: { type: sugar }
```
