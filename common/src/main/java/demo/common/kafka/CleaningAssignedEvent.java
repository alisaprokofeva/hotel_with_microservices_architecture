//также RoomStatus надо выкинуть в общщую папку и добавить как столбец в ReservationService

package demo.common.kafka;

import demo.common.model.status.RoomStatus;
import lombok.Builder;

@Builder
public record CleaningAssignedEvent (
        Long reservationId,
        Long roomId,
        Long cleanerId,
        RoomStatus status
){
}
