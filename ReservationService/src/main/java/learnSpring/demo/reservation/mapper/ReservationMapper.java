package learnSpring.demo.reservation.mapper;

import learnSpring.demo.reservation.model.Reservation;
import learnSpring.demo.reservation.model.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public Reservation toDomain(ReservationEntity reservationEntity){
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus()
        );
    }
    public ReservationEntity toEntity(Reservation reservation){
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
