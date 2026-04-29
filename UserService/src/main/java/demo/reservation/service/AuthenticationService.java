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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public AuthenticationResponse resetPasswordWithTemporaryPassword(ResetPasswordWithTempDto request) {
        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!temporaryPasswordService.validateAndUseTemporaryPassword(user, request.temporaryPassword())) {
            throw new RuntimeException("Invalid or expired temporary password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticateWithTemporaryPassword(TemporaryPasswordAuthRequest request) {
        var user = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!temporaryPasswordService.validateAndUseTemporaryPassword(user, request.temporaryPassword())) {
            throw new RuntimeException("Invalid or expired temporary password");
        }
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void changePassword(ChangePasswordRequest request, String email) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
    }

    public AuthenticationResponse refreshToken(String email) {
        log.info("Refreshing token for email: {}", email);
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("User found with role: {}", user.getRole());
        var jwtToken = jwtService.generateToken(user);
        log.info("JWT token generated for user with role: {}", user.getRole());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}