#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp dm-backend/target/dm-backend-1.0-SNAPSHOT.jar .

zip -r dm.zip  ./dm-backend-1.0-SNAPSHOT.jar ./dmDriver   ./plugin.json
