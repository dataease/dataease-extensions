#!/bin/sh
mvn clean package

cp view-3dpie-backend/target/view-3dpie-backend-1.0-SNAPSHOT.jar .

zip -r 3dpie.zip  ./view-3dpie-backend-1.0-SNAPSHOT.jar ./plugin.json

rm -f ./view-3dpie-backend-1.0-SNAPSHOT.jar
