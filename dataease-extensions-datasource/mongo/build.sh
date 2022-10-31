#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp mongo-backend/target/mongo-backend-1.0-SNAPSHOT.jar .

zip -r mongo.zip  ./mongo-backend-1.0-SNAPSHOT.jar ./mongobiDriver   ./plugin.json
