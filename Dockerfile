# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Tests are skipped during Docker build because:
# 1. Tests run in GitHub Actions CI pipeline before deployment
# 2. This ensures faster Docker image builds
# 3. Quality gates are enforced at CI level, not Docker build level
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
