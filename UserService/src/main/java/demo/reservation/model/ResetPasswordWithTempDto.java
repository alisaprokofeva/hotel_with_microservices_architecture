package demo.reservation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordWithTempDto(
        @NotNull
        @Email(message = "Некорректный формат email")
        String email,
        @NotNull
        String temporaryPassword,
        @NotNull
        String newPassword
) {
}

