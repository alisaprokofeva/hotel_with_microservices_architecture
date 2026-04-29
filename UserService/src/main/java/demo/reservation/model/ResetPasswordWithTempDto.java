package demo.reservation.model;

import jakarta.validation.constraints.NotNull;

public record ResetPasswordWithTempDto(
        @NotNull
        String email,
        @NotNull
        String temporaryPassword,
        @NotNull
        String newPassword
) {
}

