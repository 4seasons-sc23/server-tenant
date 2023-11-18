FROM docker.io/library/gradle:7-jdk19-focal AS builder
WORKDIR /build
COPY . /build
RUN gradle build -x test

FROM docker.io/library/openjdk:19-oracle

# maria
ENV MARIA_IP=maria_ip
ENV MARIA_PORT=maria_port
ENV MARIA_DATABASE=maria_database
ENV MARIA_USERNAME=maria_username
ENV MARIA_PASSWORD=maria_password
ENV MARIA_ROOT_PASSWORD=maria_root_password

# redis
ENV REDIS_IP=redis_ip
ENV REDIS_PORT=redis_port
ENV REDIS_PASSWORD=redis_password

# minio
ENV MINIO_BUCKET=your_minio_bucket
ENV MINIO_ENDPOINT=your_minio_endpoint
ENV MINIO_ACCESS_KEY=minio_access_key
ENV MINIO_SECRET_KEY=minio_secret_key

EXPOSE 8080

RUN mkdir -p /usr/local/bin
COPY --from=builder /build/build/libs/*.jar /usr/local/bin/app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${SERVER_MODE}", "-jar", "/usr/local/bin/app.jar"]
