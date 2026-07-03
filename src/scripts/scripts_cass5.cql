drop KEYSPACE if exists store;

CREATE KEYSPACE IF NOT EXISTS store
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };


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


-- phone_saindex таблица с SAI индексами для бенчмарков
drop table IF EXISTS store.phone_saindex;

CREATE TABLE IF NOT EXISTS store.phone_saindex
(
    id       bigint,
    brand    text,
    name     text,
    cost_min bigint,
    cost_max bigint,
    PRIMARY KEY (id)
);

-- Create SAI indexes
--CREATE CUSTOM INDEX IF NOT EXISTS phone_saindex_brand_idx ON store.phone_saindex (brand) USING 'StorageAttachedIndex';
CREATE CUSTOM INDEX IF NOT EXISTS phone_saindex_name_idx ON store.phone_saindex (name) USING 'StorageAttachedIndex';
--CREATE CUSTOM INDEX IF NOT EXISTS phone_saindex_cost_min_idx ON store.phone_saindex (cost_min) USING 'StorageAttachedIndex';
--CREATE CUSTOM INDEX IF NOT EXISTS phone_saindex_cost_max_idx ON store.phone_saindex (cost_max) USING 'StorageAttachedIndex';


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
--CREATE INDEX IF NOT EXISTS phone_rsindex_cost_min_idx ON store.phone_rsindex (cost_min);


-- phone_vector_dp таблица с Vector DOT_PRODUCT индексами для бенчмарков
drop table IF EXISTS store.phone_vector_dp;

CREATE TABLE IF NOT EXISTS store.phone_vector_dp
(
    id       bigint,
    brand    text,
    name     text,
    cost_min bigint,
    cost_max bigint,
    comment_vector VECTOR<FLOAT, 128>,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS phone_comment_vector ON store.phone_vector_dp (comment_vector) USING 'sai' WITH OPTIONS = { 'similarity_function': 'DOT_PRODUCT' };


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

-- phone таблица с малым количеством больших партциий (много разных кластерных ключей)
/*
drop table IF EXISTS store.display;

CREATE TABLE IF NOT EXISTS store.display
(
    size text,
    id   bigint,
    name text,
    PRIMARY KEY ((size), id)
);
*/

