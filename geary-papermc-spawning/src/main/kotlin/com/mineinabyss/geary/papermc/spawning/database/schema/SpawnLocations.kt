package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.tables.Table
import me.dvyy.sqlite.tables.View
import org.bukkit.World
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

object SpawnLocationTables {
    //TODO cache object creations
    fun locationsView(world: World): View = View(
        "spawn_locations_${world.tableSuffix}",
        """
        SELECT rtree.id as id, data.data as data, minX, minY, minZ, maxX, maxY, maxZ
        FROM ${rtree(world)} rtree
        INNER JOIN ${dataTable(world)} data ON rtree.id = data.id
        """.trimIndent(),
        involves = setOf() //TODO
    )

    fun rtree(world: World): Table = Table(
        """
        CREATE VIRTUAL TABLE IF NOT EXISTS spawn_locations_rtree_${world.tableSuffix} USING rtree(
            id, minX, maxX, minY, maxY, minZ, maxZ,
        );
        """.trimIndent()
    )


    fun dataTable(world: World): Table {
        val name = "spawn_locations_data_${world.tableSuffix}"
        return object: Table(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id INTEGER PRIMARY KEY,
                data TEXT NOT NULL
            ) STRICT;
            """.trimIndent()
        ) {
            context(tx: WriteTransaction)
            override fun create() {
                super.create()
                // Index json data
                tx.exec("CREATE INDEX IF NOT EXISTS ${name}_created_time ON $name (data ->> 'createdTime');")

                // Delete from rtree when removed from here
                tx.exec("""
                CREATE TRIGGER IF NOT EXISTS ${name}_on_delete AFTER DELETE ON $name FOR EACH ROW BEGIN 
                    DELETE FROM ${rtree(world)} WHERE id = OLD.id;
                END;
                """.trimIndent())
            }
        }
    }

    // We use uid instead of name to not deal with possible sqlite injections or having to parse anything further
    @OptIn(ExperimentalUuidApi::class)
    internal val World.tableSuffix get() = uid.toKotlinUuid().toHexString()
}
