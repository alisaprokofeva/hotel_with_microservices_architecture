let selectedRoomId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const container = document.getElementById('rooms-container');
    const loader = document.getElementById('loader');

    try {
        const rooms = await request(CONFIG.RESERVATION_SERVICE, '/rooms', 'GET');
        if (loader) loader.style.display = 'none';

        if (!rooms || rooms.length === 0) {
            container.innerHTML = `<div class="col-span-full text-center py-20 text-xl font-bold">Нет доступных номеров.</div>`;
            return;
        }

        container.innerHTML = '';
        rooms.forEach(room => {
            container.innerHTML += createRoomCard(room);
        });
    } catch (err) {
        console.error("Ошибка:", err);
        if (loader) loader.innerHTML = `<p class="text-red-500">Ошибка загрузки: ${err.message}</p>`;
    }
});

function createRoomCard(room) {
    let imageUrl = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&q=80&w=800';
    if (room.imageUrls && room.imageUrls.length > 0) imageUrl = room.imageUrls[0];

    return `
    <div class="bg-white rounded-2xl overflow-hidden room-card-shadow flex flex-col h-full border border-gray-100">
        <div class="aspect-[16/10] overflow-hidden">
            <img class="w-full h-full object-cover" src="${imageUrl}" onerror="this.src='https://via.placeholder.com/800x500'"/>
        </div>
        <div class="p-8 flex flex-col gap-5 mt-auto">
            <div class="flex justify-between items-end">
                <span class="text-3xl font-bold text-primary">$${room.price} <span class="text-sm text-gray-400 font-normal">/ night</span></span>
            </div>
            <button onclick="openDateModal(${room.id})"
                    class="w-full bg-primary text-white py-4 rounded-xl font-bold uppercase tracking-widest text-sm hover:bg-slate-800 transition-all active:scale-95">
                Забронировать
            </button>
        </div>
    </div>`;
}

function openDateModal(roomId) {
    selectedRoomId = roomId;
    const today = new Date().toISOString().split('T')[0];
    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];

    document.getElementById('start-date').value = today;
    document.getElementById('end-date').value = tomorrow;
    
    const resultDiv = document.getElementById('availability-result');
    if (resultDiv) {
        resultDiv.classList.add('hidden');
        resultDiv.innerText = '';
    }

    document.getElementById('date-modal').classList.remove('hidden');
    document.getElementById('btn-confirm-booking').onclick = executeBooking;
}

function closeDateModal() {
    document.getElementById('date-modal').classList.add('hidden');
}

async function checkAvailability() {
    const start = document.getElementById('start-date').value;
    const end = document.getElementById('end-date').value;
    const resultDiv = document.getElementById('availability-result');

    if (!start || !end) {
        alert("Пожалуйста, выберите даты.");
        return;
    }

    try {
        const response = await request(CONFIG.RESERVATION_SERVICE, '/reservation/availability/check', 'POST', {
            roomId: selectedRoomId,
            startDate: start,
            endDate: end
        });

        resultDiv.classList.remove('hidden', 'text-red-500');
        resultDiv.classList.add('text-green-500');
        resultDiv.innerText = response.message || "Номер доступен на выбранные даты!";
    } catch (err) {
        resultDiv.classList.remove('hidden', 'text-green-500');
        resultDiv.classList.add('text-red-500');
        resultDiv.innerText = "Номер занят или произошла ошибка: " + err.message;
    }
}

async function executeBooking() {
    const start = document.getElementById('start-date').value;
    const end = document.getElementById('end-date').value;

    if (!start || !end) {
        alert("Пожалуйста, выберите даты.");
        return;
    }

    try {
        await request(CONFIG.RESERVATION_SERVICE, '/reservation', 'POST', {
            roomId: selectedRoomId,
            startDate: start,
            endDate: end
        });

        alert("Бронирование успешно создано! Теперь оно доступно в вашем профиле.");
        closeDateModal();
    } catch (err) {
        alert("Ошибка бронирования: " + err.message);
    }
}