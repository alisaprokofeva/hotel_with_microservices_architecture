package demo.reservation.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record RoomRequestDto(
        @NotNull
        @Positive
        BigDecimal price,
        List<String> imageUrls
) {
}
