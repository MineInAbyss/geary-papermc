# Target selectors

Target selectors (defined with the `using` tag) specify the entity actions run on. Subskills will inherit the target selector of their parent skill, unless they specify their own.

## Event targets

[[Events]] specify their own targets based on involved entities. Since the event is carried through to subskills, they may swap back and forth between involved entities.

## Variable targets

Targets can also reference entities via variables (currently only inline). Lists of entities are also supported, and will be iterated over. On the sidebar, you will find some components for reading entities that may be used by target selectors.

Example of a list of targets defined as an inline variable:

```yaml
using: { nearbyEntities: { radius: 10 } }
```
