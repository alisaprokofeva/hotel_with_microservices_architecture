package demo.reservation.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailabilityRequestDto(
        @NotNull
        Long roomId,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate
) {
}
