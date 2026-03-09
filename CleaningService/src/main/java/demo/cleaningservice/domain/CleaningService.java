//обязательно перенести ReservationPaidEvent и CleaningAssignedEvent в отдельную папку для доступа
//как со стороны CleaningService так и со стороны ReservationService
//также CleaningStatus надо выкинуть в общщую папку и добавить как столбец в ReservationService


package demo.cleaningservice.domain;

import demo.cleaningservice.kafka.CleaningAssignedEvent;
import demo.cleaningservice.kafka.ReservationPaidEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CleaningService {

    private static final Logger log = LoggerFactory.getLogger(CleaningService.class);
    private final CleaningEntityRepository cleaningEntityRepository;

    private final KafkaTemplate<Long, CleaningAssignedEvent> kafkaTemplate;

    @Value("cleaning.events")
    private String cleaningAssignedEventTopic = "cleaning-assigned-event";


    public void processCleaning(ReservationPaidEvent reservationPaidEvent) {
        log.info("Called CleaningService, service is trying to clean the room by num: {}", reservationPaidEvent.roomId());
        var reservationId = reservationPaidEvent.reservationId();
        var found = cleaningEntityRepository.findById(reservationId);
        if (found.isPresent()) {
            log.info("Cleaning reservation {} found", reservationId);
            return;
        }
        var assignedCleaning = assignCleaning(reservationId, reservationPaidEvent.roomId());
        sendCleaningAssignedEvent(assignedCleaning);
    }


    //вспомогательный метод для отправки сообщения в топик
    private void sendCleaningAssignedEvent(CleaningEntity assignedCleaning) {
        kafkaTemplate.send(
                cleaningAssignedEventTopic,
                assignedCleaning.getReservationId(),
                CleaningAssignedEvent.builder()
                        .reservationId(assignedCleaning.getReservationId())
                        .roomId(assignedCleaning.getRoomId())
                        .cleanerId(assignedCleaning.getCleanerId())
                        .build()
        );
    }

    //вспомогательный метод для подтверджения уборки в комнате и сохранения сущности уборки в номере в бд
    private CleaningEntity assignCleaning(Long reservationId, Long roomId) {
        var entity = new CleaningEntity();
        entity.setReservationId(reservationId);
        entity.setCleanerId(ThreadLocalRandom.current().nextLong(1,1000));
        entity.setStatus(CleaningStatus.CLEAR);
        entity.setRoomId(roomId);
        return cleaningEntityRepository.save(entity);
    }
}
