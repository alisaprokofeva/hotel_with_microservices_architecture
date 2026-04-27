package demo.reservation.model;

import demo.common.model.status.RoomStatus;
import demo.common.model.status.PaymentStatus;
import demo.reservation.model.status.ReservationStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
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
        RoomStatus roomStatus,
        Long etaMinutes
){
}