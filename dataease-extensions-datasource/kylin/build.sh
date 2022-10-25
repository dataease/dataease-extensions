#!/bin/sh
 mvn clean package -U -Dmaven.test.skip=true

cp kylin-backend/target/kylin-backend-1.0-SNAPSHOT.jar .

zip -r kylin.zip  ./kylin-backend-1.0-SNAPSHOT.jar ./kylinDriver   ./plugin.json
