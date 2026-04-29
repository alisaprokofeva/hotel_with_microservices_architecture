//обязательно перенести ReservationPaidEvent и CleaningAssignedEvent в отдельную папку для доступа
//как со стороны CleaningService так и со стороны ReservationService
//также RoomStatus надо выкинуть в общщую папку и добавить как столбец в ReservationService


package demo.cleaningservice.service;

import demo.cleaningservice.model.entity.CleaningEntity;
import demo.cleaningservice.model.status.CleanerStatus;
import demo.cleaningservice.repository.CleaningEntityRepository;
import demo.common.kafka.CleaningAssignedEvent;
import demo.common.kafka.ReservationPaidEvent;
import demo.common.model.status.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
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
        var assignedCleaning = assignCleaning(reservationId, reservationPaidEvent.roomId());
        sendCleaningAssignedEvent(assignedCleaning);
        log.info("Cleaning reservation {} is done", reservationId);
    }


    //вспомогательный метод для отправки сообщения в топик
    private void sendCleaningAssignedEvent(CleaningEntity assignedCleaning) {
        kafkaTemplate.send(
                cleaningAssignedEventTopic,
                assignedCleaning.getReservationId(),
                CleaningAssignedEvent.builder()
                        .reservationId(assignedCleaning.getReservationId())
                        .roomId(assignedCleaning.getRoomId())
                        .cleanerId(assignedCleaning.getId())
                        .status(assignedCleaning.getRoomStatus())
                        .etaMinutes(assignedCleaning.getEtaMinutes())
                        .build()
        );
    }

    private CleaningEntity assignCleaning(
            Long reservationId,
            Long roomId
    ) {
        List<CleaningEntity> cleaners = cleaningEntityRepository.findCleaningEntitiesByCleanerStatus(CleanerStatus.FREE);
        if(cleaners.isEmpty()){
            log.info("All cleaners are busy, room number {} need wait a few minutes", roomId);
        }
        CleaningEntity entity = cleaners.stream().min(Comparator.comparingLong(CleaningEntity::getId)).orElse(null);
        entity.setReservationId(reservationId);
        entity.setRoomId(roomId);
        entity.setRoomStatus(RoomStatus.DIRT);
        entity.setEtaMinutes(ThreadLocalRandom.current().nextLong(5,15));
        entity.setCleanerStatus(CleanerStatus.BUSY);

        return cleaningEntityRepository.save(entity);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateEta(){
        List<CleaningEntity> cleaners = cleaningEntityRepository.findCleaningEntitiesByCleanerStatus(CleanerStatus.BUSY);
        for(CleaningEntity entity: cleaners){
            if(entity.getEtaMinutes() <= 0){
                continue;
            }
            if(entity.getEtaMinutes() == 1){
                entity.setRoomStatus(RoomStatus.CLEAR);
                entity.setEtaMinutes(0L);
                sendCleaningAssignedEvent(entity);
                entity.setCleanerStatus(CleanerStatus.FREE);
                entity.setReservationId(null);
                entity.setRoomId(null);
                entity.setRoomStatus(RoomStatus.DIRT);
            }
            else{
                entity.setEtaMinutes(entity.getEtaMinutes() - 1);
                sendCleaningAssignedEvent(entity);
            }

        }
        cleaningEntityRepository.saveAll(cleaners);
    }

}
