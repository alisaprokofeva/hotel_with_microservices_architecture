document.getElementById('btn-register').onclick = async () => {
    const payload = {
        name: document.getElementById('reg-name').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value
    };

    try {
        await authApi.post('/register', payload);
        alert('Аккаунт создан! Теперь войдите.');
        switchTab('login');
    } catch (err) {
        alert('Ошибка регистрации: ' + err.message);
    }
};

document.getElementById('btn-login').onclick = async () => {
    const payload = {
        email: document.getElementById('login-email').value,
        password: document.getElementById('login-password').value
    };

    try {
        const data = await authApi.post('/authenticate', payload);
        if (data.token) {
            localStorage.setItem('token', data.token);
            window.location.href = 'rooms.html';
        }
    } catch (err) {
        alert('Ошибка входа: ' + err.message);
    }
};

document.getElementById('btn-temp-login').onclick = async () => {
    const payload = {
        email: document.getElementById('temp-email').value,
        tempPassword: document.getElementById('temp-code').value
    };

    try {
        const data = await authApi.post('/authenticate-with-temp', payload);
        if (data.token) {
            localStorage.setItem('token', data.token);
            window.location.href = 'rooms.html';
        }
    } catch (err) {
        alert('Доступ запрещен: ' + err.message);
    }
};

const btnReset = document.getElementById('btn-reset-password');
if(btnReset) {
    btnReset.onclick = async () => {
        const payload = {
            email: document.getElementById('reset-email').value,
            temporaryPassword: document.getElementById('reset-code').value,
            newPassword: document.getElementById('reset-password').value
        };

        try {
            const data = await authApi.post('/reset-password-with-temp', payload);
            if (data.token) {
                localStorage.setItem('token', data.token);
                alert('Пароль успешно сброшен!');
                window.location.href = 'rooms.html';
            } else {
                alert('Пароль сброшен! Теперь войдите с новым паролем.');
                switchTab('login');
            }
        } catch (err) {
            alert('Ошибка сброса: ' + err.message);
        }
    };
}