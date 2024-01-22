#!/usr/bin/env bash
container="sftp"
docker rm -f "$sftp"
docker run -p 22:22 --name "$container" -d atmoz/sftp root:root:::upload
sleep 3
docker logs "$container"