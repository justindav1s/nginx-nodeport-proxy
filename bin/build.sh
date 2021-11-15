#!/bin/bash

. ./env.sh


oc project $PROJECT

oc delete bc ${APP}
oc delete secret quayio-dockercfg

oc create secret docker-registry quayio-dockercfg \
  --docker-server=${QUAYIO_HOST} \
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
    -p GIT_URL=https://github.com/justindav1s/nginx-nodeport-proxy.git \
    -n $PROJECT

oc start-build -F ${APP} -n $PROJECT   