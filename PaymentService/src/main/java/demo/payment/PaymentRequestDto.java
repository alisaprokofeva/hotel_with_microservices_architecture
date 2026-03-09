//общая папка

package demo.payment;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record PaymentRequestDto(
        @NotNull
        Long reservationId,
        @NotNull
        BigDecimal amount
) {
}
