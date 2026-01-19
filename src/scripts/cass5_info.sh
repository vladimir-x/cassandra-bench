#!/bin/bash

cat /opt/apache-cassandra-5.0.5/conf/cassandra-env.sh | grep JMX_PORT=
cat /opt/apache-cassandra-5.0.5/conf/jvm-server.options | grep Xmx
cat /opt/apache-cassandra-5.0.5/conf/jvm-server.options | grep Xms
cat /opt/apache-cassandra-5.0.5/conf/cassandra.yaml | grep native_transport_port\:

cat /opt/apache-cassandra-5.0.5/conf/cassandra.yaml | grep selected_format\:
cat /opt/apache-cassandra-5.0.5/conf/cassandra.yaml | grep inherits\:
