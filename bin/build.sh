#!/bin/bash

. ./env.sh


oc project $PROJECT

oc delete bc ${APP}

oc create secret docker-registry quayio-dockercfg \
  --docker-server=${QUAYIO_REGISTRY} \
  --docker-username=${QUAYIO_USER} \
  --docker-password=${QUAYIO_PASSWORD} \
  --docker-email=${QUAYIO_EMAIL} \
  -n $PROJECT

oc new-app -f ../configuration/templates/build-config.yaml \
    -p BUILD_NAME=${APP} \
    -p APPLICATION_LABEL=${APP} \
    -p DOCKER_REGISTRY=quay.io \
    -p DOCKER_REPOSITORY=justindav1s \
    -p IMAGE_NAME=${APP} \
    -p IMAGE_VERSION=latest \
    -n $PROJECT

oc start-build -F ${APP} -n $PROJECT   