#!/bin/bash

. ./env.sh


oc project $PROJECT

oc delete bc cpu-tcp-proxy

oc new-app -f ../configuration/templates/build-config.yaml \
    -p BUILD_NAME=cpu-tcp-proxy \
    -p APPLICATION_LABEL=cpu-tcp-proxy \
    -p DOCKER_REGISTRY=nexus-dev-devops.services.theosmo.com \
    -p DOCKER_REPOSITORY=mo \
    -p IMAGE_NAME=cpu-tcp-proxy \
    -p IMAGE_VERSION=latest \
    -n $PROJECT

oc start-build -F cpu-tcp-proxy -n $PROJECT   