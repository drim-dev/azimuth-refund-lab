FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY contracts/pom.xml contracts/pom.xml
COPY contracts/src contracts/src
COPY tooling/azimuth-jvm tooling/azimuth-jvm
COPY services/refund-api/pom.xml services/refund-api/pom.xml
COPY services/refund-api/src services/refund-api/src
RUN mvn -N -DskipTests install \
    && mvn -DskipTests -f tooling/azimuth-jvm/pom.xml install \
    && mvn -DskipTests -f contracts/pom.xml install \
    && mvn -DskipTests -f services/refund-api/pom.xml package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/refund-api/target/refund-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
