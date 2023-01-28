#!/bin/bash

if [[ "$1" == "install" ]]; then
  java -jar /usr/local/lib/shadow/shadow.jar "$SHADOW_PROJECT" "$@"
else
  /bin/og-apt-get "$@"
fi