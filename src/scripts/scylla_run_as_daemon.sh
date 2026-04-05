#!/bin/bash

# скрипт корректно работает, елси подложить его в дирректорию /opt/scylladb/
bin/scylla  --io-properties-file conf/io_properties.yaml &
echo $!
