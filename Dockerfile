FROM java:latest
MAINTAINER Brian Zhao

RUN mkdir -p /root/app
COPY target/Neo4JTest-1.0-SNAPSHOT-jar-with-dependencies.jar /root/app/

WORKDIR /root/app/

CMD ["java", "-jar", "Neo4JTest-1.0-SNAPSHOT-jar-with-dependencies.jar"]