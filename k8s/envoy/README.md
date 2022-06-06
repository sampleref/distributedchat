

#### Copy/Edit files
1. Copy certs built(`certificate.crt` and `privateKey.key`) into this folder
2. Verify changes in `front-envoy*.yaml` as applicable
3. Use `front-envoy-notls.yaml` to skip SSL/HTTPS mode in Dockerfile[Line:9]

#### Envoy docker build and run

`docker build -t dchat_envoyfrontproxy:1.0 .`

#### Add admin port is required[ -p 9000:9000 ]
`docker run -it --rm -p 9443:9443 dchat_envoyfrontproxy:1.0`
