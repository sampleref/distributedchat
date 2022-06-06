FROM envoyproxy/envoy:v1.21.2

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl net-tools iputils-ping dnsutils \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /tmp/* /var/tmp/* /var/lib/apt/lists/*

COPY ./front-envoy.yaml /etc/front-envoy.yaml
COPY ./certificate.crt /etc/certificate.crt
COPY ./privateKey.key /etc/privateKey.key

RUN chmod 755 /etc/front-envoy.yaml
CMD ["/usr/local/bin/envoy", "-c", "/etc/front-envoy.yaml", "--service-cluster", "front-proxy"]
