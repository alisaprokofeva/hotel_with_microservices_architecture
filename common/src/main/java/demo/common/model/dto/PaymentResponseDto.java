//общая папка

package demo.common.model.dto;

import demo.common.model.status.PaymentStatus;
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
