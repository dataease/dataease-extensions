#!/bin/sh
mvn clean package

cp presto-backend/target/presto-backend-1.0-SNAPSHOT.jar .

zip -r presto.zip  ./presto-backend-1.0-SNAPSHOT.jar ./prestoDriver   ./plugin.json
