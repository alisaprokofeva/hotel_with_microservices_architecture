package demo.reservation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AuthenticationRequest(
        @NotNull
        @Email(message = "Некорректный формат email")
        String email,
        @NotNull
        String password
) {
}