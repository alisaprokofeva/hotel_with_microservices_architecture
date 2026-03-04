package demo.reservation.model;

public record AvailabilityResponseDto(
        String message,
        AvailabilityStatus status
){
}
