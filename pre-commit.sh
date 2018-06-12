#!/usr/bin/env bash

set -eu

yellow=$(tput setaf 3)
blue=$(tput setaf 4)
reset=$(tput sgr0)

no_docker=false
for arg in "$@"
do
  case "$arg" in
    --no-docker)
      no_docker=true
      ;;
  esac
done

if [ "$no_docker" = true ]
then
  ./gradlew clean build
else
  >&2 echo -n "$yellow"
  >&2 echo 'Running tests in docker to avoid having to install python dependencies on your host'
  >&2 echo 'To suppress this behaviour pass the --no-docker command line argument'
  >&2 echo -n "$reset"

  mkdir -p logs
  echo "${blue}Building your docker image. This will sometimes take a while...${reset}"
  echo "${blue}Tail logs/docker-build.log to see what's going on.${reset}"
  docker build -t verify-event-emitter . &> logs/docker-build.log

  echo "${blue}Running tests in docker.${reset}"
  docker run --rm -v "$PWD":/home/user verify-event-emitter ./gradlew clean build
fi

