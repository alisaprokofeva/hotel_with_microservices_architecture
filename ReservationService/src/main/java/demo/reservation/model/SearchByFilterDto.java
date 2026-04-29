package demo.reservation.model;

public record SearchByFilterDto(
        Long roomId,
        Integer pageSize,
        Integer pageNumber
) {
}
