
package demo.common.model.dto;



import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentRequestDto(
        @NotNull
        Long reservationId,
        @NotNull
        BigDecimal amount
) {
}
