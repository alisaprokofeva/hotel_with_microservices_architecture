package demo.reservation.service;

import demo.reservation.mapper.UserEntityMapper;
import demo.reservation.model.AuthenticationResponse;
import demo.reservation.model.UserResponseDto;
import demo.reservation.model.UserRole;
import demo.reservation.model.entity.UserEntity;
import demo.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final UserEntityMapper mapper;
    private final AuthenticationService authenticationService;

    @Value("${app.admin-secret-key}")
    private String adminSecretKey;


    public UserResponseDto getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return mapper.toUserResponseDto(user);
    }


    public UserEntity findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public UserResponseDto promoteToAdminWithSecret(String secretKey) {
        log.info("Attempting to promote user to admin with secret key");

        if (!secretKey.equals(adminSecretKey)) {
            log.warn("Invalid admin secret key provided");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin secret key");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Promoting user with email: {}", email);

        UserEntity user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (user.getRole() == UserRole.ADMIN) {
            log.warn("User {} is already an admin", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already an admin");
        }

        user.setRole(UserRole.ADMIN);
        repository.save(user);
        log.info("Successfully promoted user {} to ADMIN", email);

        return mapper.toUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto promoteUserToAdmin(Long userId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} attempting to promote user {} to admin", currentEmail, userId);

        UserEntity currentUser = repository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        log.info("Current user role: {}", currentUser.getRole());

        if (currentUser.getRole() != UserRole.ADMIN) {
            log.warn("User {} with role {} tried to promote another user", currentEmail, currentUser.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can promote users to admin");
        }

        UserEntity userToPromote = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (userToPromote.getRole() == UserRole.ADMIN) {
            log.warn("User {} is already an admin", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already an admin");
        }

        userToPromote.setRole(UserRole.ADMIN);
        repository.save(userToPromote);
        log.info("Successfully promoted user {} to ADMIN by {}", userId, currentEmail);

        return mapper.toUserResponseDto(userToPromote);
    }

    @Transactional
    public AuthenticationResponse promoteToAdminWithSecretAndRefreshToken(String secretKey) {
        log.info("Starting promotion to admin with token refresh");
        UserResponseDto userResponse = promoteToAdminWithSecret(secretKey);
        log.info("User promoted, role in response: {}", userResponse.role());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting fresh token for email: {}", email);

        AuthenticationResponse tokenResponse = authenticationService.refreshToken(email);
        log.info("New token generated, checking if it contains admin role");

        return tokenResponse;
    }

    public AuthenticationResponse refreshToken(String email) {
        return authenticationService.refreshToken(email);
    }
}