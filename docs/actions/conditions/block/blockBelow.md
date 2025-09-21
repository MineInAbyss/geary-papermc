---
template: configurable.html
title: "blockBelow"
type: Block
desc: Checks material of the block below
params:
  allow:
    type: List<Material>
    desc: List of materials to allow, if empty ignored
    default: "[]"
  deny:
    type: List<Material>
    desc: List of materials to deny
    default: "[]"
---

## Examples

```yaml
blockBelow:
  allow: [ stone, grass_block ]
```

```yaml
blockBelow:
  deny: [ water, lava ]
```
