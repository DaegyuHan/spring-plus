# Step 1: Use a base image with Java installed
FROM openjdk:17-jdk-alpine

# Step 2: Set a working directory inside the container
WORKDIR /app

# Step 3: Copy the jar file into the container
COPY build/libs/expert-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 7070

# Step 5: Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]