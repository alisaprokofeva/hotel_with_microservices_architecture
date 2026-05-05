document.addEventListener('DOMContentLoaded', () => {
    initProfile();
    loadBookings();
});

let roomCache = {};

async function initProfile() {
    try {
        const user = await userApi.get('/me');
        const name = user.name || user.username || "Гость";
        document.getElementById('profile-name').innerText = name;
        document.getElementById('user-name-display').innerText = name;
        document.getElementById('profile-email').innerText = user.email || "";
    } catch (e) { 
        console.error("Ошибка загрузки профиля:", e); 
        document.getElementById('profile-name').innerText = "Ошибка загрузки";
    }
}

async function loadRooms() {
    if (Object.keys(roomCache).length > 0) return;
    try {
        const rooms = await resApi.get('/rooms');
        rooms.forEach(r => {
            roomCache[r.id] = r;
        });
    } catch (e) {
        console.error("Ошибка загрузки комнат:", e);
    }
}

async function loadBookings() {
    const container = document.getElementById('bookings-container');
    const loader = document.getElementById('booking-loader');

    try {
        await loadRooms();
        const bookings = await resApi.get('/reservation');

        if (loader) loader.remove();
        container.innerHTML = '';

        if (!bookings || bookings.length === 0) {
            container.innerHTML = `<div class="text-center py-20 opacity-50">У вас нет бронирований</div>`;
            return;
        }

        bookings.forEach(booking => {
            const room = roomCache[booking.roomId];
            const imageUrl = room && room.imageUrls && room.imageUrls.length > 0 && String(room.imageUrls[0]).startsWith('http') ? room.imageUrls[0] : 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&q=80&w=800';
            
            const isPending = booking.reservationStatus === 'PENDING';
            let actions = '';
            
            if (isPending) {
                actions = `
                    <div class="flex gap-2 mt-4">
                        <button onclick="approveReservation(${booking.id})" class="bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded text-xs font-bold transition-all shadow">Оплатить</button>
                        <button onclick="openEditModal(${booking.id}, ${booking.roomId}, '${booking.startDate}', '${booking.endDate}')" class="bg-slate-600 hover:bg-slate-500 text-white px-4 py-2 rounded text-xs font-bold transition-all shadow">Изменить</button>
                        <button onclick="cancelReservation(${booking.id})" class="bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded text-xs font-bold transition-all shadow">Отменить</button>
                    </div>
                `;
            }

            const statusColors = {
                'PENDING': 'text-yellow-400',
                'APPROVED': 'text-green-400',
                'CANCELLED': 'text-red-400'
            };
            const statusColor = statusColors[booking.reservationStatus] || 'text-gray-400';
            const roomStatusHtml = booking.roomStatus ? ` | Уборка: <span class="text-[#F8F4F0]">${booking.roomStatus}</span>` : '';
            const etaHtml = booking.etaMinutes ? ` | ETA: <span class="text-[#F8F4F0]">${booking.etaMinutes} мин</span>` : '';

            const card = document.createElement('div');
            card.className = "bg-white/5 p-6 rounded-2xl border border-white/10 mb-4 flex gap-6 items-center premium-shadow transform transition duration-300 hover:scale-[1.01]";
            card.innerHTML = `
                <div class="w-32 h-24 rounded-lg overflow-hidden flex-shrink-0">
                    <img src="${imageUrl}" class="w-full h-full object-cover opacity-80" alt="Room image">
                </div>
                <div class="flex-grow">
                    <h3 class="text-xl font-bold">Номер Люкс (ID: ${booking.roomId})</h3>
                    <p class="text-xs text-gray-400 mb-1">Резервация: #${booking.id} | <span class="${statusColor} font-bold">${booking.reservationStatus}</span>${roomStatusHtml}${etaHtml}</p>
                    <p class="text-xs text-gray-300">Заезд: <span class="text-white">${booking.startDate}</span> — Выезд: <span class="text-white">${booking.endDate}</span></p>
                    ${actions}
                </div>
                <div class="text-right">
                    <span class="text-2xl font-bold text-[#F8F4F0]">$${booking.amount}</span>
                </div>
            `;
            container.appendChild(card);
        });

    } catch (error) {
        console.error("Ошибка загрузки бронирований:", error);
        if (loader) loader.remove();
        container.innerHTML = `
            <div class="text-center py-10 border border-red-500/30 rounded-2xl bg-red-500/5">
                <p class="text-red-400 font-bold">ОШИБКА ПОДКЛЮЧЕНИЯ</p>
                <p class="text-[10px] text-gray-500 mt-2">${error.message}</p>
                <button onclick="location.reload()" class="mt-4 text-xs underline">Попробовать снова</button>
            </div>`;
    }
}

function openEditModal(id, roomId, start, end) {
    document.getElementById('edit-reservation-id').value = id;
    document.getElementById('edit-room-id').value = roomId;
    document.getElementById('edit-start-date').value = start;
    document.getElementById('edit-end-date').value = end;
    document.getElementById('edit-modal').classList.remove('hidden');
}

function hideEditModal() {
    document.getElementById('edit-modal').classList.add('hidden');
}

async function submitEditReservation() {
    const id = document.getElementById('edit-reservation-id').value;
    const roomId = document.getElementById('edit-room-id').value;
    const start = document.getElementById('edit-start-date').value;
    const end = document.getElementById('edit-end-date').value;

    if (!start || !end) {
        alert("Пожалуйста, заполните даты");
        return;
    }

    try {
        await request(CONFIG.RESERVATION_SERVICE, `/reservation/${id}/update`, 'PUT', {
            roomId: parseInt(roomId),
            startDate: start,
            endDate: end
        });
        alert("Бронирование успешно обновлено!");
        hideEditModal();
        loadBookings();
    } catch (err) {
        alert("Ошибка редактирования: " + err.message);
    }
}

async function approveReservation(id) {
    try {
        await resApi.post(`/reservation/${id}/approve`);
        alert("Бронирование успешно оплачено!");
        loadBookings();
    } catch (err) {
        alert("Ошибка оплаты: " + err.message);
    }
}

async function cancelReservation(id) {
    if (!confirm("Вы уверены, что хотите отменить бронирование?")) return;
    try {
        await resApi.delete(`/reservation/${id}/cancel`);
        alert("Бронирование отменено.");
        loadBookings();
    } catch (err) {
        alert("Ошибка отмены: " + err.message);
    }
}

async function generatePass() {
    const btn = document.getElementById('btn-generate-pass');
    const display = document.getElementById('pass-display');
    
    btn.innerText = "Генерация...";
    btn.disabled = true;

    try {
        const response = await authApi.post('/generate-temp-passwords');
        if (response && response.temporaryPasswords && response.temporaryPasswords.length > 0) {
            display.innerText = response.temporaryPasswords[0];
            display.classList.remove('hidden');
        } else {
            alert("Не удалось сгенерировать пароли.");
        }
    } catch (err) {
        alert("Ошибка: " + err.message);
    } finally {
        btn.innerText = "Получить временный код";
        btn.disabled = false;
    }
}

function showChangePasswordModal() {
    document.getElementById('change-password-modal').classList.remove('hidden');
}

function hideChangePasswordModal() {
    document.getElementById('change-password-modal').classList.add('hidden');
}

async function changePassword() {
    const oldPass = document.getElementById('old-password').value;
    const newPass = document.getElementById('new-password').value;
    
    if(!oldPass || !newPass) {
        alert("Заполните оба поля");
        return;
    }

    try {
        await authApi.post('/change-password', {
            currentPassword: oldPass,
            newPassword: newPass,
            confirmationPassword: newPass
        });
        alert("Пароль успешно изменён!");
        hideChangePasswordModal();
    } catch (err) {
        alert("Ошибка изменения пароля: " + err.message);
    }
}

function showAdminModal() {
    document.getElementById('admin-modal').classList.remove('hidden');
}

function hideAdminModal() {
    document.getElementById('admin-modal').classList.add('hidden');
}

async function promoteToAdmin() {
    const secret = document.getElementById('admin-secret').value;
    if(!secret) return;
    
    try {
        const response = await userApi.post(`/promote-to-admin?secret=${encodeURIComponent(secret)}`);
        if(response.token) {
            localStorage.setItem('token', response.token);
        }
        alert("Права администратора получены!");
        hideAdminModal();
    } catch(err) {
        alert("Ошибка: " + err.message);
    }
}

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}