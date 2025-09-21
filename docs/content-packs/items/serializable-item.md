# Serializable Item

We often need to reference an item in Geary, usually through our own SerializableItemStack. The item may inherit from other plugins or be configured in-place (ex referencing another prefab in a recipe, or defining the item itself.)

All properties are optional, setting a property will override the vanilla value, below is an example of all properties:

```yaml
# You may use a vanilla item or inherit from another item system 
type: stone # (1)!
prefab:  String  # (2)!
crucibleItem:  String 
oraxenItem:  String 
itemsadderItem:  String 

# Override item properties
amount: 2
customModelData: 42
displayName: "<bold><red>Fancy stone" # (3)!
lore:
  - "Lore line 1"
  - "<red>Formatted line 2"
unbreakable: false
damage: 0
enchantments:
  - enchant: minecraft:sharpness # (4)!
    level: 3
itemFlags: [ HIDE_ENCHANTS ] # (5)!
attributeModifiers:
  - attribute: GENERIC_ATTACK_DAMAGE # (6)!
    modifier:
      name: customAttribute
      amount: 1.0
      operation: ADD_NUMBER # (7)!
potionData:
  type: REGEN # (8)!
  extended: true
  upgraded: true
color: '#fffff' # (9)!
knowledgeBookRecipes: [ "minecraft:gold_ingot_from_nuggets" ] # (10)!
```

1. Vanilla item type, may be prefixed with `minecraft:` or written in all caps, ex: `minecraft:stone` or `STONE`
2. Geary prefab to read item off of. Must itself have a `geary:set.item` component
3. All text lines support MiniMessage formatting
4. Minecraft namespaced key for an enchant.
5. List of [ItemFlag](https://jd.papermc.io/paper/1.20/org/bukkit/inventory/ItemFlag.html) values
6. [Attribute](https://jd.papermc.io/paper/1.20/org/bukkit/attribute/Attribute.html) to modify
7. Type of operation (add, multiple, etc), see [AttributeModifier.Operation](https://jd.papermc.io/paper/1.20/org/bukkit/attribute/AttributeModifier.Operation.html)
8. Potion type, see [PotionType](https://jd.papermc.io/paper/1.20/org/bukkit/potion/PotionType.html)
9. Potion color, in the form `#RRGGBB` or `r, g, b` with 0-255 values
10. List of recipes to unlock in a knowledge book, use Minecraft namespaced keys for recipes.
