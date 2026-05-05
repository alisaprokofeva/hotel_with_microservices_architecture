#!/bin/bash

echo "Запуск фронтенда на порту 63342..."
cd Frontend
python3 -m http.server 63342
