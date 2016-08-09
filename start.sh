#!/usr/bin/env bash

docker run --name=neo4j_indexing -e NEO4J_IP=192.168.2.10 -e NEO4J_PASS=$NEO4J_PASS -d -v ./link_data.txt:/root/app/link_data.txt:ro "bmzhao/neo4j:latest"