# 1. build
FROM gradle:8.3-jdk17 AS build

WORKDIR /app
COPY . .

RUN gradle clean bootJar --no-daemon

# 2. prepare for runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
