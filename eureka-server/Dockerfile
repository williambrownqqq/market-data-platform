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

WORKDIR /workspace

RUN apk add --no-cache maven tree

COPY . .

# Show the source file structure
RUN echo "=== Source file structure ===" && \
    find eureka-server/src -name "*.java" && \
    echo ""

# Build
RUN mvn clean package -pl eureka-server -am -DskipTests

# Show what was built
RUN echo "=== Target directory ===" && \
    ls -lah /workspace/eureka-server/target/ && \
    echo ""

# Show what's IN the JAR
RUN cd /workspace/eureka-server/target && \
    MAIN_JAR=$(ls -1 *.jar | grep -v ".original" | head -1) && \
    echo "=== Contents of $MAIN_JAR ===" && \
    jar -tf "$MAIN_JAR" | head -50 && \
    echo "..." && \
    echo "" && \
    echo "=== Searching for .class files ===" && \
    jar -tf "$MAIN_JAR" | grep "\.class$" | head -20 && \
    echo "" && \
    echo "=== Searching for EurekaServer ===" && \
    jar -tf "$MAIN_JAR" | grep -i eureka && \
    echo "" && \
    echo "=== MANIFEST.MF ===" && \
    jar -xf "$MAIN_JAR" META-INF/MANIFEST.MF && \
    cat META-INF/MANIFEST.MF

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/eureka-server*.jar /app/app.jar
CMD ["java", "-jar", "app.jar"]