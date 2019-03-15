#!/bin/bash

pushd ..
./gradlew clean assemble
popd
pwd
cp ../build/libs/torrboll-*.jar .
mkdir resources
mkdir resources/db
cp ../src/main/resources/db/* resources/db/

cp ../../service-wrapper/bin/InstallTestWrapper-NT.bat InstallService.bat
cp ../../service-wrapper/bin/UninstallTestWrapper-NT.bat UninstallService.bat
cp ../../service-wrapper/bin/StartTestWrapper-NT.bat StartService.bat
cp ../../service-wrapper/bin/StopTestWrapper-NT.bat StopService.bat
cp ../../service-wrapper/bin/wrapper-windows-x86-32.exe torrboll.exe
cp ../../service-wrapper/conf/wrapper.conf torrboll.conf
mkdir lib
cp -r ../../service-wrapper/lib/wrapper* lib/
