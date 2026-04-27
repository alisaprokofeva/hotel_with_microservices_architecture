package demo.cleaningservice.kafka;


import demo.cleaningservice.service.CleaningService;
import demo.common.kafka.ReservationPaidEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

@EnableKafka
@Configuration
@AllArgsConstructor
public class CleaningKafkaConsumer {

    private final CleaningService cleaningService;

    @KafkaListener(
            topics = "${reservation-paid-topic}",
            containerFactory = "reservationPaidEventListenerFactory"
    )
    public void listen(ReservationPaidEvent reservationPaidEvent) {
        cleaningService.processCleaning(reservationPaidEvent);
    }
}
