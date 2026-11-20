# ---------- Stage 1: build the JAR ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Download dependencies first (cached unless pom.xml changes)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Stage 2: run the JAR ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]