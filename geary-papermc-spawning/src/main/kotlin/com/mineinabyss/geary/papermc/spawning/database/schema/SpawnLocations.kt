package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.tables.View

val SpawnLocations = View(
    "SpawnLocations",
    """
    SELECT tree.id as id, tb.data as data, minX, minY, minZ, maxX, maxY, maxZ
    FROM SpawnLocationsRtree tree
    INNER JOIN SpawnLocationsTable tb ON tree.id = tb.id
    """.trimIndent(),
    involves = setOf(SpawnLocationsRtree, SpawnLocationsTable)
)
