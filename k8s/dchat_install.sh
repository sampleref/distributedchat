#!/bin/bash
kubectl create namespace dchat-setup

kubectl apply -f dchat_serviceweb.yaml -n dchat-setup
kubectl apply -f dchat_envoy.yaml -n dchat-setup
