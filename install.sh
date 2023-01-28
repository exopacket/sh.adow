#!/bin/bash

#mvn install

mkdir /usr/local/lib/shadow
cp "${PWD}/target/shadow-1.0-jar-with-dependencies.jar" /usr/local/lib/shadow/shadow.jar

mv /bin/apt /bin/og-apt
mv "${PWD}/apt.sh" /bin/apt
chmod 0755 /bin/apt

mv /bin/apt-get /bin/og-apt-get
mv "${PWD}/apt-get.sh" /bin/apt-get
chmod 0755 /bin/apt-get

mv "${PWD}/shadow-cli.sh" /bin/shadow
chmod 0755 /bin/shadow