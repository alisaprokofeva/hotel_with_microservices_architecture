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
                reservationEntity.getPaymentId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getAmount(),
                reservationEntity.getReservationStatus(),
                reservationEntity.getPaymentStatus(),
                reservationEntity.getCleaningStatus(),
                reservationEntity.getCleanerId()
        );
    }
    public ReservationEntity toEntity(ReservationRequestDto reservationRequestDto) {
        ReservationEntity entity = new ReservationEntity();
        entity.setRoomId(reservationRequestDto.roomId());
        entity.setStartDate(reservationRequestDto.startDate());
        entity.setEndDate(reservationRequestDto.endDate());
        return entity;
    }
}
