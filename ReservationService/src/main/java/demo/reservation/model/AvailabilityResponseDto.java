package demo.reservation.model;

import demo.reservation.model.status.AvailabilityStatus;
import lombok.Builder;

@Builder
public record AvailabilityResponseDto(
        String message,
        AvailabilityStatus status
){
}
