package demo.reservation.service;

import demo.reservation.external.PaymentHttpClient;
import demo.reservation.external.PaymentRequestDto;
import demo.reservation.external.PaymentResponseDto;
import demo.reservation.kafka.CleaningAssignedEvent;
import demo.reservation.kafka.ReservationPaidEvent;
import demo.reservation.model.status.CleaningStatus;
import demo.reservation.model.status.PaymentStatus;
import demo.reservation.model.ReservationResponseDto;
import jakarta.persistence.EntityNotFoundException;
import demo.reservation.model.ReservationRequestDto;
import demo.reservation.model.entity.ReservationEntity;
import demo.reservation.mapper.ReservationMapper;
import demo.reservation.model.SearchByFilterDto;
import demo.reservation.repository.ReservationRepository;
import demo.reservation.model.status.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService availabilityService;
    private final PaymentHttpClient paymentHttpClient;

    public ReservationService(ReservationRepository repository, ReservationMapper reservationMapper, ReservationAvailabilityService availabilityService, PaymentHttpClient paymentHttpClient){
        this.repository = repository;
        this.reservationMapper = reservationMapper;
        this.availabilityService = availabilityService;
        this.paymentHttpClient = paymentHttpClient;
    }

    public ReservationResponseDto createReservation(ReservationRequestDto reservationToCreate) {
        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var entityToSave = reservationMapper.toEntity(reservationToCreate);
        entityToSave.setReservationStatus(ReservationStatus.PENDING);
        entityToSave.setPaymentStatus(PaymentStatus.NOT_APPLICABLE);

        //временные заглушки (тк отсутствует список комнат+цен, и юзер сервис)
        entityToSave.setUserId(ThreadLocalRandom.current().nextLong(1, 100));
        entityToSave.setAmount(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(3000, 10000)));

        var savedEntity = repository.save(entityToSave);
        return reservationMapper.toResponseDto(savedEntity);
    }

    public ReservationResponseDto getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(()->
                new EntityNotFoundException
                ("Not found reservation by id "+id));

        return reservationMapper.toResponseDto(reservationEntity);
    };


    public List<ReservationResponseDto> searchAllByFilter(
            SearchByFilterDto filter
    ) {
        int pageSize = filter.pageSize() != null ? filter.pageSize():10; //лучше вынести в applicationProperties
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber():0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream().map(reservationMapper::toResponseDto).toList();
    }

    public ReservationResponseDto updateReservation
            (Long id,
             ReservationRequestDto reservationToUpdate
    ) {
        var reservationEntity = repository.findById(id).orElseThrow(()-> new EntityNotFoundException
                ("Not found reservation by id"+id));

        if(reservationEntity.getReservationStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Can't modify reservation: status = "+reservationEntity.getReservationStatus());
        }
        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var reservationToSave = reservationMapper.toEntity((reservationToUpdate));
        reservationToSave.setReservationStatus(ReservationStatus.PENDING);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setUserId(reservationEntity.getUserId());
        reservationToSave.setAmount(reservationEntity.getAmount());
        reservationToSave.setPaymentId(reservationEntity.getPaymentId());
        //тут может все лечь из-за пустых полей с клинером и статусом
        var updateReservation = repository.save(reservationToSave);
        return reservationMapper.toResponseDto(updateReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found reservation by id: "+id));
        if(reservation.getReservationStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Can't cancel approved reservation. Contact" +
                    " with manager");
        }
        if(reservation.getReservationStatus().equals(ReservationStatus.CANCELLED)){
            throw new IllegalArgumentException("Can't cancel the reservation." +
                    " Reservation was already cancelled");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public ReservationResponseDto approveReservation(Long id) {

        var reservationEntity = repository.findById(id).orElseThrow(()-> new EntityNotFoundException
                ("Not found reservation by id"+id));

        if(reservationEntity.getReservationStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Can't approve reservation = "+reservationEntity.getReservationStatus());
        }
        var isAvailableToApprove = availabilityService.isReservationAvailable(reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate());
        if(!isAvailableToApprove){
            throw new IllegalArgumentException("Can't approve reservation because of conflict");
        }
        reservationEntity.setPaymentStatus(PaymentStatus.PENDING_PAYMENT);
        reservationEntity =  repository.save(reservationEntity);
        //makePayment
        var request = new PaymentRequestDto(
                id,
                reservationEntity.getAmount()
        );
        log.info("Approving reservation: id={}", id);
        var response = processPayment(id, request);
        log.info("Successfully approved reservation: id={}", id);
        return reservationMapper.toResponseDto(response);
    }

    public ReservationEntity processPayment(Long id, PaymentRequestDto request) {
        var reservationEntity = getReservationOrThrow(id);

        //??
        if(!reservationEntity.getPaymentStatus().equals(PaymentStatus.PENDING_PAYMENT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment status is not PENDING");
        }

        var response = paymentHttpClient.createPayment(PaymentRequestDto.builder()
                .reservationId(reservationEntity.getId())
                .amount(reservationEntity.getAmount())
                .build());

//        var status = response.paymentStatus().equals(PaymentStatus.PAID)
//                ? ReservationStatus.APPROVED
//                : ReservationStatus.PENDING;
        //тест
        var status = ReservationStatus.APPROVED;
        reservationEntity.setReservationStatus(status);
        reservationEntity.setPaymentId(response.paymentId());
//        reservationEntity.setPaymentStatus(response.paymentStatus());

        return repository.save(reservationEntity);
    }


    //метод для paymentService, в параметрах также должен быть респонс с номером комнаты и номером бронирования
    //(основной id)
    //в моменте после прохода оплаты сервиса в основной бд в данном номере будет стоять что номер грязный
    //так
    private void sendReservationPaidEvent(ReservationEntity reservationEntity ) {}

    public void processCleaningAssigned(CleaningAssignedEvent cleaningAssignedEvent) {
        var reservation = getReservationOrThrow(cleaningAssignedEvent.reservationId());
        if(!reservation.getPaymentStatus().equals(PaymentStatus.PAID)){
            throw new IllegalStateException("Can't process cleaning assigned reservation");
        }
        reservation.setCleaningStatus(CleaningStatus.CLEAR);
        reservation.setCleanerId(cleaningAssignedEvent.cleanerId());
        repository.save(reservation);
    }

    //вспомогательный приватный метод для processCleaningAssigned
    private ReservationEntity getReservationOrThrow(Long id){
        var reservationEntityOptional = repository.findById(id);
        return reservationEntityOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

}
