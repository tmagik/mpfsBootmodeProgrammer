#!/bin/sh
echo "Use 'mvn package -DskipTests' when you want to skip tests"

mvn -version && mvn clean && mvn package && mvn compile assembly:single
