# Етап 1: Збірка проекту через Gradle
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Копіюємо файли конфігурації Gradle для кешування залежностей
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts* .

# Надаємо права на виконання wrapper і завантажуємо залежності
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# Копіюємо вихідний код і збираємо JAR без тестів
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Етап 2: Легкий runtime-контейнер
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# У Gradle скомпільований jar потрапляє в build/libs/
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]