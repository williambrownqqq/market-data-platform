## Multi-stage build for Eureka Server
#FROM eclipse-temurin:17-jdk-alpine AS build
#
## Set working directory
#WORKDIR /app
#
## Copy Maven wrapper and pom.xml
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#
## Make mvnw executable
#RUN chmod +x mvnw
#
## Download dependencies (cached layer if pom.xml doesn't change)
#RUN ./mvnw dependency:go-offline -B
#
## Copy source code
#COPY src src
#
## Build the application
#RUN ./mvnw clean package -DskipTests
#
## Runtime stage
#FROM eclipse-temurin:17-jre-alpine
#
## Set working directory
#WORKDIR /app
#
## Copy jar from build stage
#COPY --from=build /app/target/*.jar app.jar
#
## Expose Eureka port
#EXPOSE 8761
#
## Add health check
#HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
#  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1
#
## Set JVM options for containerized environment
#ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
#
## Run the application
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Copy entire project
COPY . .

# Build only this module
RUN mvn clean package -pl eureka-server -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/eureka-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
