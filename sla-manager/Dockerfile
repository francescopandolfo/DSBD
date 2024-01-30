#FROM mysql

#ENV MYSQL_DATABASE slamanager
#ENV MYSQL_USER francesco
#ENV MYSQL_PASSWORD pandolfo
#ENV MYSQL_ROOT_PASSWORD toor
#ENV MYSQL_HOST localhost

#EXPOSE 3306

FROM maven:3.6-openjdk-17 AS build

#ENV spring.datasource.url jdbc:mysql://localhost:3306/slamanager?useSSL=false
#ENV spring.datasource.username francesco
#ENV spring.datasource.password pandolfo
#ENV spring.jpa.properties.hibernate.dialect org.hibernate.dialect.MySQLDialect
#ENV spring.jpa.hibernate.ddl-auto update

COPY src /home/app/src
COPY pom.xml /home/app
#RUN mvn -f /home/app/pom.xml clean install
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:17-alpine
COPY --from=build /home/app/target/sla-manager-0.0.1-SNAPSHOT.jar /home/app-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/home/app-SNAPSHOT.jar"]