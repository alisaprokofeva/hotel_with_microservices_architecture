package demo.reservation.model;

import lombok.Builder;

import java.util.List;

@Builder
public record GenerateTemporaryPasswordsResponse(
        Long userId,
        List<String> temporaryPasswords,
        String message
) {
}

