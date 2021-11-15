#!/bin/bash

. ./env.sh


oc new-project $PROJECT

oc apply -f ../configuration/secrets/bitbucket-ro.yaml -n $PROJECT

oc apply -f ../configuration/secrets/docker-p2p.yaml  -n $PROJECT
