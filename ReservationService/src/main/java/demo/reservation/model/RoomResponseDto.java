package demo.reservation.model;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponseDto(
        Long id,
        BigDecimal price,
        List<String> imageUrls
) {
}
