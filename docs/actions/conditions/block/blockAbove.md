---
template: configurable.html
title: "blockAbove"
type: Block
desc: Checks material of the block above
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
blockAbove:
  allow: [ stone, grass_block ]
```

```yaml
blockAbove:
  deny: [ water, lava ]
```
