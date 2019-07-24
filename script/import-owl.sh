#!/usr/bin/env bash
cd /home/bonifacechacha/Projects/ontology-annotation/data
curl -v -F file=@autism-merged.owl -F namespace=AUTISM https://ontology-search.herokuapp.com/api/v1/term
