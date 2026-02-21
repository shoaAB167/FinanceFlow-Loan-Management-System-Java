# Stage 1: Build the application
# We use a Maven image to build the JAR file from source code.
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached if pom.xml hasn't changed)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# We use a lightweight JRE image for the final container.
FROM amazoncorretto:21
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/finance-flow-loan-management-system-0.0.1-SNAPSHOT.war app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
