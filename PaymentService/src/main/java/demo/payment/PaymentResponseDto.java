//общая папка

package demo.payment;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record PaymentResponseDto(
        @NotNull
        Long paymentId,
        @NotNull
        Long reservationId,
        @NotNull
        PaymentStatus paymentStatus,
        @NotNull
        BigDecimal amount

) {
}
