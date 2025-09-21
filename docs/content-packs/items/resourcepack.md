# Resourcepack

Geary provides a `resourcepack` component that will automatically generate custom item json files for you.

**First, enable resourcepack generation in Geary's `config.yml`:**

```yaml
resourcePack:
  generate: true
  # The path to generate the pack to from `plugins/Geary`
  # Adding .zip to path will export as a zip instead of directory
  outputPath: resourcepack.zip
  # Points to a resourcepack in zip or directory format to merge into Geary's pack
  includedPackPath: template_pack
```

**Add your resourcepack files in `plugins/Geary/template_pack`, notably you only need to add textures for custom items:**

```markdown
template_pack/
└── assets/
    └── mineinabyss/
        └── textures/
            └── item/
                └── my_texture.png
```

**Use the resourcepack component on your item:**

```yaml
set.item:
  item:
    type: stone
    customModelData: 100
resourcepack:
  textures: my_addon:item/my_texture
```

This will automatically generate a `resourcepack.zip` with the right customModelData pointing to your texture. You can manually serve this file with your server, or use a plugin like [Packy](https://github.com/MineInAbyss/Packy) to automatically combine generated packs for you.
