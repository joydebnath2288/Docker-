# ==============================================================================
# Multi-stage Dockerfile for Student Management System
# ==============================================================================
#
# Docker best practice: "Multi-stage build"
# - Stage 1 (builder): Uses Maven + JDK 17 to compile Java code and build the .war
# - Stage 2 (runner): Uses lightweight Tomcat 10.1 image to run the app
#
# This keeps the final image small and secure because build tools (like Maven)
# are not included in the final production runtime container.
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Application
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy pom.xml first to download and cache Maven dependencies
# (Docker caches this step, so dependencies aren't re-downloaded unless pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code
COPY src ./src

# Package the project into a WAR file (skipping unit tests during packaging for speed)
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Run the Application on Tomcat 10.1 (Jakarta EE 10 / Servlet 6.0)
# ------------------------------------------------------------------------------
FROM tomcat:10.1-jdk17

# Remove default sample webapps (ROOT, examples, docs) to keep Tomcat clean
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the compiled WAR file from the builder stage as ROOT.war
# This deploys the app to the root path: http://localhost:8085/
COPY --from=builder /app/target/StudentManagement.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat listens on port 8080 inside the container
EXPOSE 8080

# Start Tomcat in foreground mode
CMD ["catalina.sh", "run"]
