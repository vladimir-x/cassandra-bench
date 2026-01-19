#!/bin/bash

sed -i '789s/.*/      inherits: trie/'     /opt/apache-cassandra-5.0.5/conf/cassandra.yaml 
sed -i '1171s/.*/  selected_format: bti/'  /opt/apache-cassandra-5.0.5/conf/cassandra.yaml 

#/opt/apache-cassandra-5.0.5/bin/cqlsh  localhost 9045 -f ../main/resources/scripts.cql
