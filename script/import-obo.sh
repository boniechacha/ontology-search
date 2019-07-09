#!/usr/bin/env bash
cd /home/bonifacechacha/Projects/ontology-annotation/data
curl -v -F file=@hp.obo http://localhost:8080/api/v1/term