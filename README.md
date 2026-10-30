# xtra-storage-service

## Overview
`xtra-storage-service` is the unified object storage subsystem for the Xtra platform. It encapsulates interactions with **MinIO S3-compatible Object Storage**, providing secure bucket management, file uploads with unique namespace generation, download streaming, and presigned time-limited URL generation. It also integrates directly with `xtra-url-service` to shorten generated presigned URLs for client consumption.

---

## Technical Specifications
- **Framework**: Spring Boot `3.5.3`
- **Spring Cloud**: `2025.0.0`
- **Java Version**: `17`
- **Port**: `9009`
- **Object Storage**: MinIO (`localhost:9000`)
- **Database**: PostgreSQL (`localhost:5432/xtra`)
- **Key Dependencies**:
  - `io.minio:minio:8.5.17`
  - `org.springframework.boot:spring-boot-starter-web`
  - `org.springframework.boot:spring-boot-starter-data-jpa`
  - `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
  - `org.postgresql:postgresql`
  - `org.projectlombok:lombok`

---

## Architecture & Storage Flow

```
Upload Request (dir, MultipartFile)
       │
       ▼
StorageController.uploadFile()
       │
       ├── 1. Ensure MinIO bucket exists (creates if absent)
       ├── 2. Generate unique object key: "{dir}/{UUID}_{filename}"
       ├── 3. Upload object bytes to MinIO with original Content-Type
       ├── 4. Generate 7-day presigned GET URL from MinIO Client
       ├── 5. POST to xtra-url-service (http://localhost:9003/api/v1/xtra/url/create)
       │      └── Returns compact short URL + ID
       └── 6. Return FileResponse:
              - filePath (MinIO object key)
              - originalUrl (full presigned URL)
              - shortUrl (shortened link)
              - urlId
```

---

## API Endpoints Reference (`/api/v1/xtra/storage`)

| Method | Endpoint | Request Details | Response |
| :--- | :--- | :--- | :--- |
| `POST` | `/uploadFile` | `@RequestPart String dir`, `@RequestPart MultipartFile file` | `FileResponse` (JSON) |
| `GET` | `/getFileUrl` | `@RequestHeader("filePath") String filePath` | String (Presigned MinIO URL valid for 7 days) |
| `DELETE` | `/delete` | `@RequestHeader("filePath") String filePath` | HTTP 200 OK |

---

## Configuration Reference
From `src/main/resources/application.yaml`:
```yaml
server:
  port: 9009
  tomcat:
    max-swallow-size: -1

spring:
  application:
    name: xtra-storage-service
  datasource:
    url: jdbc:postgresql://localhost:5432/xtra
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
  servlet:
    multipart:
      enabled: true
      max-file-size: 3GB
      max-request-size: 3GB

minio:
  url: http://localhost:9000
  access-key: admin
  secret-key: m!n!02708@
  bucket-name: testing
  post-dir: posts
  profile-dir: profile_pic

eureka:
  client:
    service-url:
      defaultZone: http://localhost:9002/eureka
```

---

## Prerequisites
- **MinIO S3 Server** running at `localhost:9000` with configured credentials (`admin` / `m!n!02708@`).
- **PostgreSQL 14+** running at `localhost:5432/xtra`.
- **xtra-discovery-service** running at `http://localhost:9002`.
- **xtra-url-service** accessible for generating short URL mappings.

---

## Running the Service

### Using Gradle Wrapper:
```bash
# Windows
.\gradlew.bat bootRun

# Linux/macOS
./gradlew bootRun
```

### Build Executable JAR:
```bash
.\gradlew.bat clean build -x test
java -jar build/libs/xtra-storage-service-0.0.1-SNAPSHOT.jar
```

