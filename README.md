# Distributed Chat

Sample Demo Application to demonstrate use of Envoy Proxy with Netty to serve html and websockets

Built to run with Kubernetes support, can be verified in local with Minikube

## Create sample .crt and .key  [in certs/localhost]
`openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout privateKey.key -out certificate.crt`

#### With alt_names in certs as needed
`openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout privateKey.key -out certificate.crt -extensions req_ext -config ./openssl-san.cnf`

#### Export SSL Cert and Key Path to Run(No K8S SSL mode)
`export SSL_CERT_PATH=<path to cert file above>`  
`export SSL_KEY_PATH=<path to key file above>`

## Build and Run With Docker

#### Build jar
`mvn clean install`

#### Build Docker
`docker build -t distributedchat_demo:1.0 .`

#### Run Docker(Standalone docker mode)
`docker run -it --rm -p 8080:8080 --name=dchatdemo distributedchat_demo:1.0`

#### If using k8s mode/minikube
Refer _README.md in k8s folder_
