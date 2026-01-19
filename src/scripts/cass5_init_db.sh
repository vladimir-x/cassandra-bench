#!/bin/bash

/opt/apache-cassandra-5.0.5/bin/cqlsh  localhost 9045 -e "drop KEYSPACE if exists store;"

rm -rf /opt/apache-cassandra-5.0.5/data/data/store/

/opt/apache-cassandra-5.0.5/bin/cqlsh  localhost 9045 -f ../main/resources/scripts.cql
