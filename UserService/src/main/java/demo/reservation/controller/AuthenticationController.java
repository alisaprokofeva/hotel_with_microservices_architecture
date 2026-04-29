package demo.reservation.controller;

import demo.reservation.model.*;
import demo.reservation.service.AuthenticationService;
import demo.reservation.service.TemporaryPasswordService;
import demo.reservation.model.entity.UserEntity;
import demo.reservation.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final TemporaryPasswordService temporaryPasswordService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid UserRequestDto request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }


    @PostMapping("/generate-temp-passwords")
    public ResponseEntity<GenerateTemporaryPasswordsResponse> generateTemporaryPasswords() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var tempPasswords = temporaryPasswordService.generateTemporaryPasswords(user);

        return ResponseEntity.ok(GenerateTemporaryPasswordsResponse.builder()
                .userId(user.getId())
                .temporaryPasswords(tempPasswords)
                .message("10 temp password generated. Save them")
                .build());
    }

    @PostMapping("/authenticate-with-temp")
    public ResponseEntity<AuthenticationResponse> authenticateWithTemp(
            @RequestBody @Valid TemporaryPasswordAuthRequest request
    ) {
        return ResponseEntity.ok(service.authenticateWithTemporaryPassword(request));
    }

    @PostMapping("/reset-password-with-temp")
    public ResponseEntity<AuthenticationResponse> resetPasswordWithTemporaryPassword(
            @RequestBody @Valid ResetPasswordWithTempDto request
    ) {
        return ResponseEntity.ok(service.resetPasswordWithTemporaryPassword(request));
    }


    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        service.changePassword(request, email);
        return ResponseEntity.ok().build();
    }
}