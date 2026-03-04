package demo.reservation.model;

import demo.reservation.model.status.AvailabilityStatus;

public record AvailabilityResponseDto(
        String message,
        AvailabilityStatus status
){
}
