FROM openjdk:jre-slim
WORKDIR /opt/docker
ADD --chown=daemon:daemon opt /opt
USER daemon
ENTRYPOINT ["/opt/docker/bin/uberv2"]
CMD []
