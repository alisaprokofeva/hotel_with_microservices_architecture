package demo.reservation.model;

import jakarta.validation.constraints.NotNull;

public record TemporaryPasswordAuthRequest(
        @NotNull
        String email,
        @NotNull
        String temporaryPassword
) {
}

