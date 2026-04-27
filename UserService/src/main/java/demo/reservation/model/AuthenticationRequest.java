package demo.reservation.model;

public record AuthenticationRequest(
        String email,
        String password
) {
}