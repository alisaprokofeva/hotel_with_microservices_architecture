package demo.reservation.service;

import demo.reservation.model.AuthenticationRequest;
import demo.reservation.model.AuthenticationResponse;
import demo.reservation.model.ChangePasswordRequest;
import demo.reservation.model.ResetPasswordWithTempDto;
import demo.reservation.model.UserRequestDto;
import demo.reservation.model.TemporaryPasswordAuthRequest;
import demo.reservation.model.entity.UserEntity;
import demo.reservation.model.UserRole;
import demo.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TemporaryPasswordService temporaryPasswordService;


    public AuthenticationResponse register(UserRequestDto request) {
        var user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Сброс пароля с использованием временного пароля
     * @param request данные для сброса пароля
     * @return AuthenticationResponse с JWT токеном
     */
    public AuthenticationResponse resetPasswordWithTemporaryPassword(ResetPasswordWithTempDto request) {
        // 1. Находим пользователя по email, а не по ID
        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Проверяем временный пароль (метод validateAndUseTemporaryPassword удалит его после использования)
        if (!temporaryPasswordService.validateAndUseTemporaryPassword(user, request.temporaryPassword())) {
            throw new RuntimeException("Invalid or expired temporary password");
        }

        // 3. Хешируем и сохраняем новый основной пароль
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);

        // 4. Генерируем токен, чтобы пользователь сразу вошел в систему
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Аутентификация с использованием временного пароля
     * @param request email и временный пароль
     * @return AuthenticationResponse с JWT токеном
     */
    public AuthenticationResponse authenticateWithTemporaryPassword(TemporaryPasswordAuthRequest request) {
        // Находим пользователя по email
        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем временный пароль
        if (!temporaryPasswordService.validateAndUseTemporaryPassword(user, request.temporaryPassword())) {
            throw new RuntimeException("Invalid or expired temporary password");
        }

        // Генерируем JWT токен
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Изменение пароля для аутентифицированного пользователя
     * @param request текущий пароль и новый пароль
     * @param email email пользователя из токена
     */
    public void changePassword(ChangePasswordRequest request, String email) {
        // Находим пользователя по email
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Проверяем, что новый пароль отличается от текущего
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Обновляем пароль
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
    }
}