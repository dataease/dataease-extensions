#!/bin/sh
mvn clean package

cp view-symbolmap-backend/target/view-symbolmap-backend-1.18.0.jar .

zip -r symbolmap.zip  ./view-symbolmap-backend-1.18.0.jar ./plugin.json

rm -f ./view-symbolmap-backend-1.18.0.jar
