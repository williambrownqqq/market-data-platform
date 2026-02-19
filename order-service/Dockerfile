## Multi-stage build for Order Service
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
## Expose Order Service port
#EXPOSE 8082
#
## Add health check
#HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
#  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1
#
## Set JVM options for containerized environment
#ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
#
## Run the application
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# -------- BUILD STAGE --------
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Copy entire project (important!)
COPY . .

# Build only order-service but resolve parent from root
RUN mvn clean package -pl order-service -am -DskipTests


# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/order-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
