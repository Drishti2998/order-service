FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/order-service.jar order-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "order-service.jar"]
