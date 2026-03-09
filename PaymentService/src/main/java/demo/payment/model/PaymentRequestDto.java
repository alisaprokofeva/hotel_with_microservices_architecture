//общая папка

package demo.payment.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record PaymentRequestDto(
        @NotNull
        Long reservationId,
        @NotNull
        BigDecimal amount
) {
}
