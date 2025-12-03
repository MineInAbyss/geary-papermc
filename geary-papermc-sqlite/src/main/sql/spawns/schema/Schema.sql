CREATE VIRTUAL TABLE spawn_rtree USING rtree_i32
(
    id,
    minX,
    maxX,
    minY,
    maxY,
    minZ,
    maxZ,
);
--
CREATE TABLE IF NOT EXISTS spawn_data
(
    id       INTEGER PRIMARY KEY,
    data TEXT NOT NULL
) STRICT;
--
CREATE TRIGGER IF NOT EXISTS spawn_data_on_delete
    AFTER DELETE
    ON spawn_data
    FOR EACH ROW
BEGIN
    DELETE FROM spawn_rtree WHERE id = OLD.id;
END;
--
CREATE INDEX IF NOT EXISTS spawn_data_created_time ON spawn_data (data ->> 'createdTime');
--
CREATE INDEX IF NOT EXISTS spawn_data_category ON spawn_data (data ->> 'category');
--
CREATE VIEW IF NOT EXISTS spawn_view AS
SELECT rtree.id  AS id,
       data.data AS data,
       minX,
       minY,
       minZ,
       maxX,
       maxY,
       maxZ
FROM spawn_rtree rtree
         INNER JOIN spawn_data data ON rtree.id = data.id;