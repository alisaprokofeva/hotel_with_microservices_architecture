package demo.reservation.controller;

import demo.reservation.model.AuthenticationResponse;
import demo.reservation.model.UserResponseDto;
import demo.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile() {
        log.info("Getting current user profile");
        UserResponseDto user = userService.getCurrentUser();
        log.info("Current user: id={}, email={}, role={}", user.id(), user.email(), user.role());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Void> checkUserExists(@PathVariable Long id) {
        return userService.existsById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/promote-to-admin")
    public ResponseEntity<AuthenticationResponse> promoteToAdmin(
            @RequestParam(name = "secret") String secretKey
    ) {
        log.info("Promoting current user to admin with secret key and returning new token");
        AuthenticationResponse response = userService.promoteToAdminWithSecretAndRefreshToken(secretKey);
        log.info("User promoted to admin and new token generated");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promote-to-admin-with-token")
    public ResponseEntity<AuthenticationResponse> promoteToAdminWithToken(
            @RequestParam(name = "secret") String secretKey
    ) {
        log.info("Promoting current user to admin with secret key and returning new token");
        AuthenticationResponse response = userService.promoteToAdminWithSecretAndRefreshToken(secretKey);
        log.info("User promoted to admin and new token generated");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/promote-to-admin")
    public ResponseEntity<UserResponseDto> promoteUserToAdmin(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        log.info("Admin attempting to promote user {} to admin", userId);
        UserResponseDto updatedUser = userService.promoteUserToAdmin(userId);
        log.info("User {} promoted to admin by admin", userId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken() {
        log.info("Refreshing token for current user");
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        AuthenticationResponse response = userService.refreshToken(email);
        log.info("Token refreshed for user: {}", email);
        return ResponseEntity.ok(response);
    }
}