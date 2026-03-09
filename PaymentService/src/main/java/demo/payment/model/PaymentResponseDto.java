//общая папка

package demo.payment.model;

import demo.payment.model.status.PaymentStatus;
import jakarta.validation.constraints.NotNull;

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
