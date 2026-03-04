package demo.reservation.mapper;

import demo.reservation.model.ReservationRequestDto;
import demo.reservation.model.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationRequestDto toDomain(ReservationEntity reservationEntity){
        return new ReservationRequestDto(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus()
        );
    }
    public ReservationEntity toEntity(ReservationRequestDto reservation){
        return new ReservationEntity(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                reservation.status()
        );
    }
}
