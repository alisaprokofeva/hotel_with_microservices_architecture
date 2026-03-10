//обязательно перенести ReservationPaidEvent и CleaningAssignedEvent в отдельную папку для доступа
//как со стороны CleaningService так и со стороны ReservationService
//также CleaningStatus надо выкинуть в общщую папку и добавить как столбец в ReservationService

package demo.common.kafka;

import lombok.Builder;

@Builder
public record CleaningAssignedEvent (
        Long reservationId,
        Long roomId,
        Long cleanerId
){
}
