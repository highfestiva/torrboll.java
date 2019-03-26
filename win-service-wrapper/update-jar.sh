#!/bin/bash

# clean build
rm torrboll-*.jar
pushd ..
./gradlew clean assemble
popd
pwd

# copy
mkdir tmp
cd tmp
cp ../../build/libs/torrboll-*.jar .

# unpack and modify
JAR=`ls torrboll-*.jar`
unzip $JAR
cd BOOT-INF/classes/
cat application.properties | sed 's/src\/main\///' > app.props
mv app.props application.properties
cd ../..
jar --update --file torrboll-0.0.1-SNAPSHOT.jar BOOT-INF/classes/application.properties

# compress and cleanup
mv $JAR ..
cd ..
rm -Rf tmp
