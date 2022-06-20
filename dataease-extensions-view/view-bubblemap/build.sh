#!/bin/sh
mvn clean package

cp view-bubblemap-backend/target/view-bubblemap-backend-1.0-SNAPSHOT.jar .

zip -r bubblemap.zip  ./view-bubblemap-backend-1.0-SNAPSHOT.jar ./plugin.json

rm -f ./view-bubblemap-backend-1.0-SNAPSHOT.jar
