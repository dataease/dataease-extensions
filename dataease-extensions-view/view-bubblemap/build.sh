#!/bin/sh
mvn clean package

cp view-bubblemap-backend/target/view-bubblemap-backend-1.18.0.jar .

zip -r bubblemap.zip  ./view-bubblemap-backend-1.18.0.jar ./plugin.json

rm -f ./view-bubblemap-backend-1.18.0.jar
