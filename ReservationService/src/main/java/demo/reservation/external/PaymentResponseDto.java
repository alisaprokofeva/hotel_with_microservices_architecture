//общая папка

package demo.reservation.external;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
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
