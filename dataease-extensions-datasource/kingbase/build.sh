#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp kingbase-backend/target/kingbase-backend-1.0-SNAPSHOT.jar .

zip -r kingbase.zip  ./kingbase-backend-1.0-SNAPSHOT.jar ./kingbaseDriver   ./plugin.json
