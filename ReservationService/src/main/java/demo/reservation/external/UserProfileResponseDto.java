package demo.reservation.external;

public record UserProfileResponseDto(
        Long id,
        String name,
        String email,
        String role
) {
}
