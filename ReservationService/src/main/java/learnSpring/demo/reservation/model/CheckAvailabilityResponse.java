package learnSpring.demo.reservation.model;

public record CheckAvailabilityResponse (
        String message,
        AvailabilityStatus status
){
}
