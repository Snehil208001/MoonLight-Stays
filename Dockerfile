# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and dependency:go-offline to cache dependencies
COPY airBnbApp/pom.xml ./airBnbApp/
RUN mvn -f airBnbApp/pom.xml dependency:go-offline

# Copy the source code and compile
COPY airBnbApp/src ./airBnbApp/src
RUN mvn -f airBnbApp/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the generated JAR file
COPY --from=build /app/airBnbApp/target/application.jar app.jar

# Expose port (Render automatically routes web traffic)
EXPOSE 5000

# Run Spring Boot app with dynamic server port mapped to Railway's $PORT env var
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-5000} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar app.jar"]

