#!/bin/sh
mvn clean package

cp maxcompute-backend/target/maxcompute-backend-1.0-SNAPSHOT.jar .

zip -r maxcompute.zip  ./maxcompute-backend-1.0-SNAPSHOT.jar ./maxcomputeDriver   ./plugin.json
