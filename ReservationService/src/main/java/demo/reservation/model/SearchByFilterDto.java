package demo.reservation.model;

public record SearchByFilterDto(
        Long roomId,
        Long userId,
        Integer pageSize,
        Integer pageNumber
){

}

