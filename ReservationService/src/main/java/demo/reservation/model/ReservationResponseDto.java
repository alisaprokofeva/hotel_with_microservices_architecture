package demo.reservation.model;

import demo.common.model.status.CleaningStatus;
import demo.common.model.status.PaymentStatus;
import demo.reservation.model.status.ReservationStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponseDto(
        @Null
        Long id,
        @NotNull
        Long userId,
        @NotNull
        Long paymentId,
        @NotNull
        Long roomId,
        @FutureOrPresent
        @NotNull
        LocalDate startDate,
        @FutureOrPresent
        @NotNull
        LocalDate endDate,
        @NotNull
        BigDecimal amount,
        ReservationStatus reservationStatus,
        PaymentStatus paymentStatus,
        CleaningStatus cleaningStatus,
        Long cleanerId
){
}