FROM amazoncorretto:17 as build
WORKDIR /app
COPY target .
EXPOSE 8080
CMD ["java", "-jar", "/app/rednet-session-service.jar"]