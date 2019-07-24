#!/usr/bin/env bash
cd ~/Projects/ontology-annotation/dev/ontology-search
mvn clean package
heroku deploy:jar target/ontology-search-1.0.0-SNAPSHOT.jar --app ontology-search --jdk 12
