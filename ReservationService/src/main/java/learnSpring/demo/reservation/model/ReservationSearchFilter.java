package learnSpring.demo.reservation.model;

public record ReservationSearchFilter(
        Long roomId,
        Long userId,
        Integer pageSize,
        Integer pageNumber
){

}

