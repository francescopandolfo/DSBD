FROM maven:3.6-openjdk-17 AS build

COPY src /usr/src/get-time-series/src
COPY pom.xml /usr/src/get-time-series
RUN mvn -f /usr/src/get-time-series/pom.xml clean package

FROM openjdk:17-alpine
COPY --from=build /usr/src/get-time-series/target/get-time-series-0.0.1-SNAPSHOT.jar /usr/get-time-series/get-time-series-0.0.1-SNAPSHOT.jar

EXPOSE 8001
ENTRYPOINT ["java", "-jar", "/usr/get-time-series/get-time-series-0.0.1-SNAPSHOT.jar"]