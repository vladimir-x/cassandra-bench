drop KEYSPACE if exists store;

CREATE KEYSPACE IF NOT EXISTS store
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        }
AND tablets = {'enabled': false};
--- tablets работает только с NetworkTopology

-- phone таблица с большим количеством маленьких партиций

drop table IF EXISTS store.phone;
CREATE TABLE IF NOT EXISTS store.phone
(
    id       bigint,
    brand    text,
    name     text,
    cost_min bigint,
    cost_max bigint,
    PRIMARY KEY (id)
);


-- phone таблица с больной партицией и кластерными ключми.
-- табличка поиска
drop table IF EXISTS store.phone_by_brand;
CREATE TABLE IF NOT EXISTS store.phone_by_brand
(
    brand text,
    name  text,
    id    bigint,
    PRIMARY KEY (brand, name)
);

