#!/bin/bash

# cat cassandra-env.sh | grep JMX_PORT=
# cat jvm-server.options | grep Xmx
# cat jvm-server.options | grep Xms
# cat cassandra.yaml | grep native_transport_port\:

/opt/apache-cassandra-4.1.10/bin/cassandra -p cass4_pid
