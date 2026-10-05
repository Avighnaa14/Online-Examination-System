FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM tomcat:11.0.26-jdk17-temurin-jammy

RUN rm -rf /usr/local/tomcat/webapps/ROOT

RUN sed -i 's/port="8080"/port="10000"/' /usr/local/tomcat/conf/server.xml

COPY --from=build /app/target/online-exam.war /usr/local/tomcat/webapps/online-exam.war

EXPOSE 10000