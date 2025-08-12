# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY rest/pom.xml rest/pom.xml
COPY calculator/pom.xml calculator/pom.xml
COPY rest/src rest/src
COPY calculator/src calculator/src

RUN --mount=type=cache,target=/root/.m2 ./mvnw clean install -q -f rest/pom.xml

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/rest/target/rest-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]