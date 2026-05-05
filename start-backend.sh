#!/bin/bash

# Остановка скрипта при ошибке сборки
set -e

echo "=== 1. Запуск инфраструктуры (БД и Kafka) ==="
docker-compose up -d

echo "Ожидание инициализации базы данных (15 секунд)..."
sleep 15

echo "=== 2. Сборка всех модулей ==="
mvn clean install -DskipTests

# Отключаем завершение при ошибке, чтобы фоновые задачи не убили скрипт сразу
set +e

echo "=== 3. Запуск микросервисов ==="

# Запускаем каждый сервис в фоне
(cd UserService && mvn spring-boot:run) &
USER_PID=$!

(cd ReservationService && mvn spring-boot:run) &
RES_PID=$!

(cd PaymentService && mvn spring-boot:run) &
PAY_PID=$!

(cd CleaningService && mvn spring-boot:run) &
CLN_PID=$!

echo "Все бэкенд сервисы запускаются..."
echo "Для полной остановки нажмите Ctrl+C (это убьет все запущенные процессы Spring Boot)"

# Функция для закрытия всех процессов при остановке скрипта
cleanup() {
    echo "Остановка сервисов..."
    kill $USER_PID $RES_PID $PAY_PID $CLN_PID 2>/dev/null
    exit 0
}

# Перехватываем сигнал остановки (Ctrl+C)
trap cleanup SIGINT SIGTERM

# Ожидаем завершения фоновых процессов
wait
