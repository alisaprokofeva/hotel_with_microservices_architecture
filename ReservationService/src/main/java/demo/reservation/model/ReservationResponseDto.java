package demo.reservation.model;

import demo.common.model.status.CleaningStatus;
import demo.common.model.status.PaymentStatus;
import demo.reservation.model.status.ReservationStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ReservationResponseDto(
        Long id,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal amount,
        ReservationStatus reservationStatus,
        PaymentStatus paymentStatus,
        CleaningStatus cleaningStatus
) {
}