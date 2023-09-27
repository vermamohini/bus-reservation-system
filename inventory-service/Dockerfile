FROM openjdk:8-jdk-alpine

#PLACEMENT OF THE EXECUTABLE [MICROSERVICE] ON THE IMAGE
COPY target/inventory-service-test-1.0.jar app.jar

#EXPOSE PORTS FOR INCOMING TRAFFIC - HOST_PORT:CONTAINER_PORT
EXPOSE 8181:8181

RUN apt-get update
RUN apt-get install -y gcc
RUN apt-get install -y curl

#ENTRYPOINT OF THE CONTAINER THROUGH THE MICROSERVICE
ENTRYPOINT ["java","-Dspring.profiles.active=test","-jar","app.jar"]