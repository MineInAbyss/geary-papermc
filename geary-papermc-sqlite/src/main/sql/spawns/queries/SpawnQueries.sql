-- getSpawnsNear
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
ORDER BY abs(minX - :x) + abs(minY - :y) + abs(minZ - :z);

-- countSpawnsInBB
SELECT count(*) AS count
FROM spawn_view
WHERE minX >= :minX
  AND minY >= :minY
  AND minZ >= :minZ
  AND maxX < :maxX
  AND maxY < :maxY
  AND maxZ < :maxZ;

-- countSpawnsInBBOfType
WITH rtree AS ( SELECT id
                FROM spawn_rtree
                WHERE minX >= :minX
                  AND minY >= :minY
                  AND minZ >= :minZ
                  AND maxX < :maxX
                  AND maxY < :maxY
                  AND maxZ < :maxZ )
SELECT count(*) AS count
FROM spawn_data AS data
         JOIN rtree ON data.id = rtree.id
WHERE data ->> 'category' = :type;

-- countNearby
SELECT count(*) AS count
FROM spawn_rtree
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
  AND (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z) <= :rad * :rad;

-- countNearbyOfType
WITH rtree AS ( SELECT id
                FROM spawn_rtree
                WHERE minX > :x - :rad
                  AND minY > :y - :rad
                  AND minZ > :z - :rad
                  AND maxX < :x + :rad
                  AND maxY < :y + :rad
                  AND maxZ < :z + :rad
                  AND (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z) <= :rad * :rad )
SELECT count(*) AS count
FROM spawn_data AS data
         JOIN rtree ON data.id = rtree.id
WHERE data ->> 'category' = :type;

-- getClosestSpawn
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
ORDER BY (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z)
LIMIT 1;

-- getClosestSpawnOfType
WITH rtree AS ( SELECT id, minX, minY, minZ
                FROM spawn_rtree
                WHERE minX > :x - :rad
                  AND minY > :y - :rad
                  AND minZ > :z - :rad
                  AND maxX < :x + :rad
                  AND maxY < :y + :rad
                  AND maxZ < :z + :rad
                ORDER BY (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z) )
SELECT data.id, data.data, rtree.minX, rtree.minY, rtree.minZ
FROM spawn_data AS data
         JOIN rtree ON data.id = rtree.id
WHERE data ->> 'category' = :type
LIMIT 1;

-- getSpawnsInBB
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX >= :minX
  AND minY >= :minY
  AND minZ >= :minZ
  AND maxX < :maxX
  AND maxY < :maxY
  AND maxZ < :maxZ;

-- getSpawnsInChunk
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX >= :x
  AND minZ >= :z
  AND maxX < :x + 16
  AND maxZ < :z + 16;

-- getSpawnsInChunkOfType
WITH rtree AS ( SELECT id, minX, minY, minZ
                FROM spawn_rtree
                WHERE minX >= :x AND minZ >= :z AND maxX < :x + 16 AND maxZ < :z + 16 )
SELECT data.id, data.data, rtree.minX, rtree.minY, rtree.minZ
FROM spawn_data AS data
         JOIN rtree ON data.id = rtree.id
WHERE data ->> 'category' = :type;

-- insertData
INSERT INTO spawn_data(id, data)
VALUES (:id, json(:data));


-- insertRtree
INSERT INTO spawn_rtree(minX, maxX, minY, maxY, minZ, maxZ)
VALUES (:x, :x, :y, :y, :z, :z);

-- deleteSpawnsOlderThan
DELETE
FROM spawn_data
WHERE data ->> 'createdTime' < :epochSeconds;

-- deleteSpawn
DELETE
FROM spawn_data
WHERE id = :id;

-- dropAll
DELETE
FROM spawn_data;