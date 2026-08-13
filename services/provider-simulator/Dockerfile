FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY contracts contracts
COPY tooling/azimuth-jvm tooling/azimuth-jvm
COPY services services
RUN mvn -B -pl services/provider-simulator -am -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 provider
WORKDIR /app
COPY --from=build /workspace/services/provider-simulator/target/provider-simulator-*.jar app.jar
USER provider
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
