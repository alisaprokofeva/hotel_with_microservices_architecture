document.addEventListener('DOMContentLoaded', () => {
    loadAdminRooms();
});

async function loadAdminRooms() {
    const container = document.getElementById('admin-rooms-container');
    const loader = document.getElementById('admin-rooms-loader');

    try {
        const rooms = await request(CONFIG.RESERVATION_SERVICE, '/rooms', 'GET');
        if (loader) loader.remove();

        container.innerHTML = '';
        if (!rooms || rooms.length === 0) {
            container.innerHTML = '<p class="text-gray-400">Нет доступных номеров.</p>';
            return;
        }

        rooms.forEach(room => {
            const imageUrl = (room.imageUrls && room.imageUrls.length > 0 && String(room.imageUrls[0]).startsWith('http')) 
                ? room.imageUrls[0] 
                : 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&q=80&w=200';

            const card = document.createElement('div');
            card.className = "bg-white/5 border border-white/10 p-4 rounded-xl flex items-center justify-between gap-4 premium-shadow";
            card.innerHTML = `
                <div class="flex items-center gap-4">
                    <img src="${imageUrl}" class="w-16 h-12 object-cover rounded-md opacity-80" alt="room">
                    <div>
                        <h3 class="font-bold text-lg">Room ID: ${room.id}</h3>
                        <p class="text-xs text-gray-400">Цена: $${room.price}</p>
                    </div>
                </div>
                <button onclick="deleteRoom(${room.id})" class="text-red-400 hover:text-red-300 font-bold text-xs uppercase tracking-widest px-4 py-2 border border-red-500/30 rounded-lg hover:bg-red-500/10 transition-all">
                    Удалить
                </button>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        console.error(err);
        if (loader) loader.innerHTML = `<p class="text-red-400">Ошибка: ${err.message}</p>`;
    }
}

async function deleteRoom(id) {
    if (!confirm("Удалить этот номер?")) return;
    try {
        await resApi.delete(`/rooms/${id}`);
        alert("Номер удален.");
        loadAdminRooms();
    } catch (err) {
        alert("Ошибка удаления: " + err.message);
    }
}

function showAddRoomModal() {
    document.getElementById('add-room-modal').classList.remove('hidden');
}

function hideAddRoomModal() {
    document.getElementById('add-room-modal').classList.add('hidden');
}

async function submitAddRoom() {
    const price = parseFloat(document.getElementById('room-price').value);
    const imagesStr = document.getElementById('room-images').value;
    
    if (!price || price <= 0) {
        alert("Введите корректную цену");
        return;
    }

    const imageUrls = imagesStr.split(',').map(s => s.trim()).filter(s => s.length > 0);

    try {
        await resApi.post('/rooms', {
            price: price,
            imageUrls: imageUrls
        });
        alert("Номер успешно добавлен!");
        hideAddRoomModal();
        loadAdminRooms();
        
        document.getElementById('room-price').value = '';
        document.getElementById('room-images').value = '';
    } catch (err) {
        alert("Ошибка добавления: " + err.message);
    }
}

async function promoteUser() {
    const userId = document.getElementById('promote-user-id').value;
    if (!userId) {
        alert("Введите ID пользователя");
        return;
    }

    try {
        await userApi.post(`/users/${userId}/promote-to-admin`);
        alert("Права администратора успешно выданы пользователю " + userId);
        document.getElementById('promote-user-id').value = '';
    } catch (err) {
        alert("Ошибка: " + err.message);
    }
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}
