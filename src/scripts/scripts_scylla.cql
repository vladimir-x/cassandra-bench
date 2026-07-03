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

-- phone_rsindex таблица с обычными индексами для бенчмарков
-- Regular Secondary Index (2i)
drop table IF EXISTS store.phone_rsindex;

CREATE TABLE IF NOT EXISTS store.phone_rsindex
(
    id       bigint,
    brand    text,
    name     text,
    cost_min bigint,
    cost_max bigint,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS phone_rsindex_name_idx ON store.phone_rsindex (name);

-- phone_counter таблица для работы со счётчиком LWT
drop table IF EXISTS store.phone_counter;
CREATE TABLE IF NOT EXISTS store.phone_counter
(
    id    bigint,
    brand text,
    counter bigint,
    version bigint,
    PRIMARY KEY (id)
);
