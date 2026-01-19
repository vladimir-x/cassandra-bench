#!/bin/bash

cat /opt/apache-cassandra-4.1.10/conf/cassandra-env.sh | grep JMX_PORT=
cat /opt/apache-cassandra-4.1.10/conf/jvm-server.options | grep Xmx
cat /opt/apache-cassandra-4.1.10/conf/jvm-server.options | grep Xms
cat /opt/apache-cassandra-4.1.10/conf/cassandra.yaml | grep native_transport_port\:

