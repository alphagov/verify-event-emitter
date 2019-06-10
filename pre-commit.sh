#!/usr/bin/env bash

set -eu

yellow=$(tput setaf 3)
blue=$(tput setaf 4)
reset=$(tput sgr0)

./gradlew clean build
./gradlew integrationTest

