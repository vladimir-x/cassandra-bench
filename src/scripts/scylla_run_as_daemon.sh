#!/bin/bash

cd /opt/scylladb
bin/scylla  --io-properties-file conf/io_properties.yaml --memory 13GiB &
echo $!
