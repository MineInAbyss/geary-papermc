---
template: configurable.html
title: "block"
type: Block
desc: Checks material of the block
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
block:
  allow: [ stone, grass_block ]
```

```yaml
block:
  deny: [ water, lava ]
```
