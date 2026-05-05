const CONFIG = {
    AUTH_SERVICE: 'http://localhost:8083/api/v1/auth',
    RESERVATION_SERVICE: 'http://localhost:8080',
    PAYMENT_SERVICE: 'http://localhost:8081/api/v1/payments',
    CLEANING_SERVICE: 'http://localhost:8082/api/v1/cleaning',
    USER_SERVICE: 'http://localhost:8083/api/v1/users'
};

 */
async function request(baseUrl, endpoint, method = 'GET', data = null) {
    const token = localStorage.getItem('token'); 

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

        const text = await response.text();
        const result = text ? JSON.parse(text) : null;
        if (!response.ok) {
            throw new Error((result && result.message) || `Ошибка сервера: ${response.status}`);
        }
        return result;
    } catch (error) {
        console.error('Fetch error:', error.message);
        throw error;
    }
}

async function apiRequest(endpoint, method = 'GET', data = null) {
    return await request(CONFIG.RESERVATION_SERVICE, endpoint, method, data);
}

const resApi = {
    get: (endpoint) => apiRequest(endpoint, 'GET'),
    post: (endpoint, data) => apiRequest(endpoint, 'POST', data),
    delete: (endpoint) => apiRequest(endpoint, 'DELETE')
};

const authApi = {
    post: (endpoint, data) => request(CONFIG.AUTH_SERVICE, endpoint, 'POST', data)
};

const userApi = {
    get: (endpoint) => request(CONFIG.USER_SERVICE, endpoint, 'GET'),
    post: (endpoint, data) => request(CONFIG.USER_SERVICE, endpoint, 'POST', data)
};