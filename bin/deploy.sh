#!/bin/bash

. ./env.sh


oc project $PROJECT

oc delete dc ${APP}
oc delete configmap ${APP}
oc delete service -l app=${APP}


sleep 10

oc create configmap ${APP} --from-file=../configuration/templates/environment-specific-config/dev/proxy_location_root.conf -n $PROJECT   

oc new-app -f ../configuration/templates/deploy-config.yaml \
   -p RESOURCE_NAME=${APP} \
   -p APP_LABEL=${APP} \
   -p IMAGE="nexus-dev-devops.services.theosmo.com/mo/cpu-tcp-proxy:latest" \
   -p REPLICAS=1 \
   -n $PROJECT

NGINX_PORT=61616
NODE_PORT=31616
oc new-app -f ../configuration/templates/service.yaml \
   -p RESOURCE_NAME=${APP}-${NODE_PORT} \
   -p APP_LABEL=${APP} \
   -p NGINX_PORT=${NGINX_PORT} \
   -p NODE_PORT=${NODE_PORT} \
   -n $PROJECT   

NGINX_PORT=61626
NODE_PORT=31626
oc new-app -f ../configuration/templates/service.yaml \
   -p RESOURCE_NAME=${APP}-${NODE_PORT} \
   -p APP_LABEL=${APP} \
   -p NGINX_PORT=${NGINX_PORT} \
   -p NODE_PORT=${NODE_PORT} \
   -n $PROJECT  

NGINX_PORT=61636
NODE_PORT=31636
oc new-app -f ../configuration/templates/service.yaml \
   -p RESOURCE_NAME=${APP}-${NODE_PORT} \
   -p APP_LABEL=${APP} \
   -p NGINX_PORT=${NGINX_PORT} \
   -p NODE_PORT=${NODE_PORT} \
   -n $PROJECT   

NGINX_PORT=61646
NODE_PORT=31646
oc new-app -f ../configuration/templates/service.yaml \
   -p RESOURCE_NAME=${APP}-${NODE_PORT} \
   -p APP_LABEL=${APP} \
   -p NGINX_PORT=${NGINX_PORT} \
   -p NODE_PORT=${NODE_PORT} \
   -n $PROJECT      