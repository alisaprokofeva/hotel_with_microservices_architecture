package demo.reservation.mapper;

import demo.reservation.model.ReservationRequestDto;
import demo.reservation.model.ReservationResponseDto;
import demo.reservation.model.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationResponseDto toResponseDto(ReservationEntity entity) {
        return ReservationResponseDto.builder()
                .id(entity.getId())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .amount(entity.getAmount())
                .reservationStatus(entity.getReservationStatus())
                .paymentStatus(entity.getPaymentStatus())
                .cleaningStatus(entity.getCleaningStatus())
                .build();
    }
    public ReservationEntity toEntity(ReservationRequestDto reservationRequestDto) {
        ReservationEntity entity = new ReservationEntity();
        entity.setStartDate(reservationRequestDto.startDate());
        entity.setEndDate(reservationRequestDto.endDate());
        return entity;
    }
}
