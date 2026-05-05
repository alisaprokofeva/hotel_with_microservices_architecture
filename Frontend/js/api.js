// Конфигурация портов твоих микросервисов
const CONFIG = {
    AUTH_SERVICE: 'http://localhost:8083/api/v1/auth',
    RESERVATION_SERVICE: 'http://localhost:8080',
    PAYMENT_SERVICE: 'http://localhost:8081/api/v1/payments',
    CLEANING_SERVICE: 'http://localhost:8082/api/v1/cleaning',
    USER_SERVICE: 'http://localhost:8083/api/v1' // Порт может быть 8080 или 8083 в зависимости от твоей настройки
};

/**
 * Базовый клиент для всех запросов
 */
async function request(baseUrl, endpoint, method = 'GET', data = null) {
    const token = localStorage.getItem('jwt_token'); // Убедись, что ключ совпадает с тем, что в логине

    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };

    if (token) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }

    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(`${baseUrl}${endpoint}`, options);
        if (response.status === 204) return null;

        const result = await response.json();
        if (!response.ok) {
            throw new Error(result.message || `Ошибка сервера: ${response.status}`);
        }
        return result;
    } catch (error) {
        console.error('Fetch error:', error.message);
        throw error;
    }
}

/**
 * АДАПТЕР: Тот самый мостик, которого тебе не хватало
 */
async function apiRequest(endpoint, method = 'GET', data = null) {
    // По умолчанию считаем, что запросы без указания сервиса идут в Reservation
    return await request(CONFIG.RESERVATION_SERVICE, endpoint, method, data);
}

// Обертки для удобства
const resApi = {
    get: (endpoint) => apiRequest(endpoint, 'GET'),
    post: (endpoint, data) => apiRequest(endpoint, 'POST', data),
    delete: (endpoint) => apiRequest(endpoint, 'DELETE')
};