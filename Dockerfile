FROM docker.io/library/openjdk:19-oracle

ENV MARIA_IP=maria_ip
ENV MARIA_PORT=maria_port
ENV MARIA_DATABASE=maria_database
ENV MARIA_USERNAME=maria_username
ENV MARIA_PASSWORD=maria_password
ENV MARIA_ROOT_PASSWORD=maria_root_password
ENV REDIS_IP=redis_ip
ENV REDIS_PORT=redis_port
ENV REDIS_PASSWORD=redis_password

EXPOSE 8080

RUN ./gradlew clean  build -x test --refresh-dependencies
ARG JAR_FILE=build/libs/*.jar

RUN mkdir -p /usr/local/bin

COPY ${JAR_FILE} /usr/local/bin/app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${SERVER_MODE}", "-jar", "/usr/local/bin/app.jar"]
