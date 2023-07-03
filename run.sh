#!/bin/sh

JDK_JAVA_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED" mvn clean compile exec:java -D{webdriver.http.factory}={jdk-http-client} -D"exec.mainClass"="com.tolstoy.censorship.twitter.checker.app.Start"

