FROM eclipse-temurin:17.0.7_7-jdk-alpine
MAINTAINER paul.nthusi@thepalladiumgroup.com
COPY target/cs-api-1.0-SNAPSHOT.jar cs-api-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/cs-api-1.0-SNAPSHOT.jar"]