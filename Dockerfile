FROM eclipse-temurin:21.0.2_13-jdk

WORKDIR app
COPY currentUserResources/* currentUserResources/
COPY src .
COPY pom.xml .
COPY target/steg0vault-0.0.1-SNAPSHOT.jar target/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/app.jar"]