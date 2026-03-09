//обязательно перенести ReservationPaidEvent и CleaningAssignedEvent в отдельную папку для доступа
//как со стороны CleaningService так и со стороны ReservationService
//также CleaningStatus надо выкинуть в общщую папку и добавить как столбец в ReservationService
package demo.cleaningservice.kafka;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReservationPaidEvent(
        Long reservationId,
        Long roomId
) {
}
