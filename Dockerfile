FROM openjdk:11-jdk

COPY certs /tmp/certs/
COPY target/dchatdemoapp.jar /tmp/dchatdemoapp.jar
COPY webapp /tmp/webapp/

ENV SSL_CERT_PATH /tmp/certs/localhost/certificate.crt
ENV SSL_KEY_PATH /tmp/certs/localhost/privateKey.key

WORKDIR /tmp/
CMD ["java","-agentlib:jdwp=transport=dt_socket,address=*:5999,server=y,suspend=n","-jar","/tmp/dchatdemoapp.jar"]
