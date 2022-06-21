#!/bin/sh
mvn clean package

cp view-symbolmap-backend/target/view-symbolmap-backend-1.0-SNAPSHOT.jar .

zip -r symbolmap.zip  ./view-symbolmap-backend-1.0-SNAPSHOT.jar ./plugin.json

rm -f ./view-symbolmap-backend-1.0-SNAPSHOT.jar
