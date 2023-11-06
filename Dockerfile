FROM openjdk:19-oracle

ENV MARIA_IP=mariadb
ENV MARIA_PORT=3306
ENV MARIA_DATABASE=your_maria_database
ENV MARIA_USERNAME=your_maria_username
ENV MARIA_PASSWORD=your_maria_password
ENV MARIA_ROOT_PASSWORD=your_maria_root_password
ENV REDIS_IP=redis
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=your_redis_password 

EXPOSE 8080

ARG JAR_FILE=build/libs/*.jar

RUN mkdir -p /usr/local/bin

COPY ${JAR_FILE} /usr/local/bin/app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${SERVER_MODE}", "-jar", "/usr/local/bin/app.jar"]
