# Stage 1: Build stage using JDK 25 and maven wrapper
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Grant execution rights on the maven wrapper (making sure it's executable in Linux)
RUN chmod +x mvnw

# Resolve dependencies (go-offline) to cache them
RUN ./mvnw dependency:go-offline -B

# Copy source code and build package
COPY src/ ./src/
RUN ./mvnw package -DskipTests -B

# Stage 2: Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built jar file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
