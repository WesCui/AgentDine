FROM openjdk:11-jre-slim

WORKDIR /app

# Copy the built jar
COPY target/sky-server-1.0-SNAPSHOT.jar app.jar

# Copy skill files
COPY src/main/resources/skills/ /app/skills/

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
