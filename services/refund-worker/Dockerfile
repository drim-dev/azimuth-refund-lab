FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY contracts contracts
COPY tooling/azimuth-jvm tooling/azimuth-jvm
COPY services/refund-worker services/refund-worker
RUN mvn -N -DskipTests install \
    && mvn -f tooling/azimuth-jvm/pom.xml -DskipTests install \
    && mvn -f contracts/pom.xml -DskipTests install \
    && mvn -f services/refund-worker/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/refund-worker/target/refund-worker-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
