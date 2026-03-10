package demo.reservation.kafka;

import demo.common.kafka.CleaningAssignedEvent;
import demo.reservation.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@EnableKafka
@Component
@AllArgsConstructor
public class ReservationKafkaConsumer {

    private final ReservationService reservationService;

    @KafkaListener(
        topics = "${cleaning-assigned-topic}",
            containerFactory = "cleaningAssignedEventListenerFactory"
    )
    public void listen(CleaningAssignedEvent cleaningAssignedEvent) {
        reservationService.processCleaningAssigned(cleaningAssignedEvent);
    }
}
