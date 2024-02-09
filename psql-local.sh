#!/usr/bin/env bash
docker-compose --file docker-compose.dependencies.yml run --rm --env PGPASSWORD=cas postgres psql -h postgres -U cas -d cas
