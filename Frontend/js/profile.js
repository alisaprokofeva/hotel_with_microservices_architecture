// Конфигурация
const CONFIG = {
    RESERVATION_SERVICE: 'http://localhost:8080',
};

// Универсальный запрос
async function request(baseUrl, endpoint, method = 'GET', data = null) {
    // ПРОВЕРЬ: В login.js ты должна сохранять токен именно так: localStorage.setItem('token', ...)
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

    // Ставим таймаут на запрос 5 секунд, чтобы не было вечной загрузки
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);
    options.signal = controller.signal;

    try {
        const response = await fetch(`${baseUrl}${endpoint}`, options);
        clearTimeout(timeoutId);

        if (response.status === 204) return null;
        const result = await response.json();

        if (!response.ok) throw new Error(result.message || 'Ошибка сервера');
        return result;
    } catch (error) {
        clearTimeout(timeoutId);
        if (error.name === 'AbortError') throw new Error('Сервер слишком долго не отвечает');
        throw error;
    }
}

// Инициализация при загрузке
document.addEventListener('DOMContentLoaded', () => {
    initProfile();
    loadBookings();
});

function initProfile() {
    const rawData = localStorage.getItem('user');
    if (!rawData) return;
    try {
        const user = JSON.parse(rawData);
        const name = user.name || user.username || "Гость";
        document.getElementById('profile-name').innerText = name;
        document.getElementById('user-name-display').innerText = name;
        document.getElementById('profile-email').innerText = user.email || "";
    } catch (e) { console.error(e); }
}

async function loadBookings() {
    const container = document.getElementById('bookings-container');
    const loader = document.getElementById('booking-loader');

    try {
        // ВАЖНО: У тебя в контроллере /reservation, значит вызываем /reservation/my
        const bookings = await request(CONFIG.RESERVATION_SERVICE, '/reservation/my', 'GET');

        if (loader) loader.remove(); // Убираем крутилку
        container.innerHTML = '';

        if (!bookings || bookings.length === 0) {
            container.innerHTML = `<div class="text-center py-20 opacity-50">У вас нет бронирований</div>`;
            return;
        }

        bookings.forEach(booking => {
            const card = document.createElement('div');
            card.className = "bg-white/5 p-6 rounded-2xl border border-white/10 mb-4 flex justify-between items-center";
            card.innerHTML = `
                <div>
                    <h3 class="text-xl font-bold">${booking.roomName || 'Номер'}</h3>
                    <p class="text-xs text-gray-400">ID: #${booking.id}</p>
                </div>
                <div class="text-right">
                    <span class="text-2xl font-bold">$${booking.totalPrice}</span>
                </div>
            `;
            container.appendChild(card);
        });

    } catch (error) {
        console.error("Ошибка загрузки:", error);
        if (loader) loader.remove(); // ПРИНУДИТЕЛЬНО УБИРАЕМ КРУТИЛКУ ПРИ ОШИБКЕ
        container.innerHTML = `
            <div class="text-center py-10 border border-red-500/30 rounded-2xl bg-red-500/5">
                <p class="text-red-400 font-bold">ОШИБКА ПОДКЛЮЧЕНИЯ</p>
                <p class="text-[10px] text-gray-500 mt-2">${error.message}</p>
                <button onclick="location.reload()" class="mt-4 text-xs underline">Попробовать снова</button>
            </div>`;
    }
}

// Заглушка для выхода
function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}