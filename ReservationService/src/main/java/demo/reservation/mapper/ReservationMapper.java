package demo.reservation.mapper;

import demo.reservation.model.ReservationRequestDto;
import demo.reservation.model.ReservationResponseDto;
import demo.reservation.model.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationResponseDto toResponseDto(ReservationEntity reservationEntity){
        return new ReservationResponseDto(
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
                null,
                null,
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                null
        );
    }
}
