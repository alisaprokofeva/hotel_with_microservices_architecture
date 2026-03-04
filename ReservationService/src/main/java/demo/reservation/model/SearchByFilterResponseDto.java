package demo.reservation.model;

public record SearchByFilterResponseDto(
        Long roomId,
        Long userId,
        Integer pageSize,
        Integer pageNumber
){

}

