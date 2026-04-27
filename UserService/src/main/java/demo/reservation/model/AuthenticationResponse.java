package demo.reservation.model;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token
) {
}