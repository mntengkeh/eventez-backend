# 1. Dependency Changes

The current `build.gradle.kts` uses `spring-boot-starter-jdbc`. To support JPA entities,
repositories, and specifications, **replace** the JDBC starter with JPA:

```kotlin
// REMOVE:
implementation("org.springframework.boot:spring-boot-starter-jdbc")

// ADD:
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
```

Also add the `kotlin("plugin.jpa")` plugin for no-arg constructors:

```kotlin
plugins {
    // ... existing plugins
    kotlin("plugin.jpa") version "2.2.21"
}
```

Update `application.yaml`:

```yaml
spring:
  application:
    name: eventez
  datasource:
    url: jdbc:postgresql://localhost:5432/eventez
    username: ${DB_USERNAME:eventez}
    password: ${DB_PASSWORD:eventez}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: always

server:
  servlet:
    session:
      timeout: 24h
```
