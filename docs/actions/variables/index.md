# Variables

Variables are supported by certain actions and let you dynamically read data in skills. Each variable specifies a type, followed by a camelCased name, and may be specified in one of three ways:

## Inline

Since all types are just component references, they may be defined like any serializable component:

```yaml
vars:
  - string name: "Just a string"
  - int age: 42
  - geary:playSound extinguish:
      sound: minecraft:entity.generic.extinguish_fire
```

## References

Variables may reference other variables defined before them (or soon data stored on entities).

```yaml
vars:
  - string name: $otherName
```

## Derived

Derived variables run an event that reads data on a target. These are useful for getting data from your events. See [[Derived Variables]] for a list of options.

```yaml
using: itemHolder
vars:
  # Read the location of itemHolder
  - derived location targetLoc:
      read.location: { }
```
