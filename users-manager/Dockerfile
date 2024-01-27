FROM maven:3.6-openjdk-17 AS build

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:17-alpine
#ARG [spring.datasource.url]="jdbc:mysql://mysqldbb:3306/dsbd-pandolfo?serverTimezone=UTC"
COPY --from=build /home/app/target/users-manager-0.0.1-SNAPSHOT.jar /home/app-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","/home/app-SNAPSHOT.jar"]