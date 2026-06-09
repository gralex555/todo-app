# ============ ЭТАП 1: СБОРКА ============
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Копируем файлы сборки Maven (для кеширования зависимостей)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Скачиваем зависимости (этот слой будет закеширован)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем jar, пропустив тесты
RUN ./mvnw clean package -DskipTests -B

# ============ ЭТАП 2: ЗАПУСК ============
FROM eclipse-temurin:25-jre

WORKDIR /app

# Копируем готовый jar из этапа сборки
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]