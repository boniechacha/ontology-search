#!/usr/bin/env bash
cd /home/bonifacechacha/Projects/ontology-annotation/data
curl -v -F file=@autism-merged.owl -F namespace=GENO http://localhost:8080/api/v1/term