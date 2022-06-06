## Kubernetes Setup Instructions:

#### Install/Use Kubernetes setup via kubectl

#### Build Envoy Proxy
1. cd envoy
2. Follow README.md in this folder

#### Replace below variable(s) with actual values in dchat_envoy.yaml
1. <EXTERNAL_IP> - External IP Address of host machine to be reachable from clients

                     - Example: 192.168.10.1    

#### If using Minikube load images build into minikube repo
Example: `minikube image load dchat_envoyfrontproxy:1.0`

Remove if any changes and load again as above `minikube image rm dchat_envoyfrontproxy:1.0`

#### Run script
1. `dchat_install.sh`


#### Port Forward if using Minikube
`kubectl port-forward -n dchat-setup service/dchat-service-envoy 9443:9443`
