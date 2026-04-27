package demo.reservation.service;

import demo.common.kafka.CleaningAssignedEvent;
import demo.common.kafka.ReservationPaidEvent;
import demo.common.model.dto.PaymentRequestDto;
import demo.common.model.dto.PaymentResponseDto;
import demo.common.model.status.PaymentStatus;
import demo.common.model.status.RoomStatus;
import demo.reservation.external.PaymentHttpClient;
import demo.reservation.external.UserHttpClient;
import demo.reservation.model.ReservationResponseDto;
import demo.reservation.model.entity.RoomEntity;
import demo.reservation.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import demo.reservation.model.ReservationRequestDto;
import demo.reservation.model.entity.ReservationEntity;
import demo.reservation.mapper.ReservationMapper;
import demo.reservation.model.SearchByFilterDto;
import demo.reservation.repository.ReservationRepository;
import demo.reservation.model.status.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;
    private final RoomRepository roomRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService availabilityService;
    private final PaymentHttpClient paymentHttpClient;
    private final UserHttpClient userHttpClient;
    private final KafkaTemplate<Long, ReservationPaidEvent> kafkaTemplate;

    @Value("${reservation-paid-topic}")
    private String reservationPaidTopic;

    @Value("${cleaning-assigned-topic}")
    private String cleaningAssignedTopic;

    public ReservationService(
            ReservationRepository repository,
            ReservationMapper reservationMapper,
            ReservationAvailabilityService availabilityService,
            PaymentHttpClient paymentHttpClient,
            UserHttpClient userHttpClient,
            KafkaTemplate<Long, ReservationPaidEvent> kafkaTemplate,
            RoomRepository roomRepository
    ){
        this.repository = repository;
        this.reservationMapper = reservationMapper;
        this.availabilityService = availabilityService;
        this.paymentHttpClient = paymentHttpClient;
        this.userHttpClient = userHttpClient;
        this.kafkaTemplate = kafkaTemplate;
        this.roomRepository = roomRepository;
    }


    public ReservationResponseDto createReservation(
            ReservationRequestDto reservationToCreate,
            String authorizationHeader
    ) {

        validateDates(reservationToCreate.startDate(), reservationToCreate.endDate());

        var room = roomRepository.findById(reservationToCreate.roomId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Room with id " + reservationToCreate.roomId() + " not found"
                ));

        var entityToSave = reservationMapper.toEntity(reservationToCreate);
        entityToSave.setUserId(resolveCurrentUserId(authorizationHeader));

        entityToSave.setReservationStatus(ReservationStatus.PENDING);
        entityToSave.setRoom(room);
        entityToSave.setAmount(calculateTotalAmount(room, reservationToCreate.startDate(), reservationToCreate.endDate()));

        var savedEntity = repository.save(entityToSave);
        return reservationMapper.toResponseDto(savedEntity);
    }

    private Long resolveCurrentUserId(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is required");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header must use Bearer token");
        }

        try {
            var currentUser = userHttpClient.getCurrentUser(authorizationHeader);
            if (currentUser == null || currentUser.id() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot resolve user from token");
            }
            return currentUser.id();
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for current user");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service is unavailable");
        }
    }


    private BigDecimal calculateTotalAmount(
            RoomEntity room,
            LocalDate startDate,
            LocalDate endDate
    ) {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return room.getPrice().multiply(BigDecimal.valueOf(nights));
    }


    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reservation cannot be in the past");
        }
    }

    public ReservationResponseDto getReservationById(Long id, String authorizationHeader) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(()->
                new EntityNotFoundException
                        ("Not found reservation by id "+id));
        ensureOwnerAccess(reservationEntity, authorizationHeader);

        return reservationMapper.toResponseDto(reservationEntity);
    };

    public List<ReservationResponseDto> searchAllByFilter(
            SearchByFilterDto filter,
            String authorizationHeader
    ) {
        Long currentUserId = resolveCurrentUserId(authorizationHeader);
        int pageSize = filter.pageSize() != null ? filter.pageSize():10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber():0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                currentUserId,
                pageable
        );

        return allEntities.stream().map(reservationMapper::toResponseDto).toList();
    }

    public ReservationResponseDto updateReservation(
            Long id,
            ReservationRequestDto reservationToUpdate,
            String authorizationHeader
    ) {

        var reservationEntity = repository.findById(id).orElseThrow(()-> new EntityNotFoundException
                ("Not found reservation by id"+id));
        ensureOwnerAccess(reservationEntity, authorizationHeader);

        ensureCanBeApproved(reservationEntity);
        validateDates(reservationToUpdate.startDate(), reservationToUpdate.endDate());

        if (!reservationEntity.getRoom().getId().equals(reservationToUpdate.roomId())) {
            var newRoom = roomRepository.findById(reservationToUpdate.roomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found"));
            reservationEntity.setRoom(newRoom);
        }

        reservationEntity.setStartDate(reservationToUpdate.startDate());
        reservationEntity.setEndDate(reservationToUpdate.endDate());
        reservationEntity.setAmount(calculateTotalAmount(
                reservationEntity.getRoom(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate()));

        checkRoomAvailability(reservationEntity);
        var updated = repository.save(reservationEntity);
        return reservationMapper.toResponseDto(updated);
    }

    @Transactional
    public void cancelReservation(Long id, String authorizationHeader) {
        var reservation = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found reservation by id: "+id));
        ensureOwnerAccess(reservation, authorizationHeader);

        if(reservation.getReservationStatus() == ReservationStatus.APPROVED){
            throw new IllegalStateException("Can't cancel approved reservation. Contact" +
                    " with manager");
        }
        if(reservation.getReservationStatus() == ReservationStatus.CANCELLED){
            throw new IllegalArgumentException("Can't cancel the reservation." +
                    " Reservation was already cancelled");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public ReservationResponseDto approveReservation(Long id, String authorizationHeader) {

        var reservationEntity = repository.findById(id).orElseThrow(()-> new EntityNotFoundException
                ("Not found reservation by id"+id));
        ensureOwnerAccess(reservationEntity, authorizationHeader);

        ensureCanBeApproved(reservationEntity);
        checkRoomAvailability(reservationEntity);
        prepareForPayment(reservationEntity);

        log.info("Approving reservation: id={}", id);
        var request = createPaymentRequest(reservationEntity);
        var updatedEntity = processPayment(id, request);
        log.info("Successfully approved reservation: id={}", id);
        return reservationMapper.toResponseDto(updatedEntity);
    }

    private PaymentRequestDto createPaymentRequest(ReservationEntity reservationEntity) {
        return new PaymentRequestDto(
                reservationEntity.getId(),
                reservationEntity.getAmount()
        );
    }

    private void prepareForPayment(ReservationEntity reservationEntity) {
        reservationEntity.setPaymentStatus(PaymentStatus.PENDING_PAYMENT);
        repository.save(reservationEntity);
    }

    private void checkRoomAvailability(ReservationEntity reservationEntity) {
        var isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoom().getId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate());

        if(!isAvailableToApprove){
            throw new IllegalArgumentException("Can't approve reservation because of conflict");
        }
    }

    private void ensureCanBeApproved(ReservationEntity reservationEntity) {
        if(reservationEntity.getReservationStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException
                    ("Can't approve reservation = "+reservationEntity.getReservationStatus());
        }
    }

    private void ensureOwnerAccess(ReservationEntity reservationEntity, String authorizationHeader) {
        Long currentUserId = resolveCurrentUserId(authorizationHeader);
        if (!currentUserId.equals(reservationEntity.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own reservation");
        }
    }

    public ReservationEntity processPayment(
            Long id,
            PaymentRequestDto request
    ) {
        var reservationEntity = getReservationOrThrow(id);
        if (reservationEntity.getPaymentStatus() != PaymentStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment status is not PENDING");
        }
        log.info("Sending payment request to external service for reservation {}", id);
        var response = paymentHttpClient.createPayment(request);

        var status = (response.paymentStatus().equals(PaymentStatus.PAID))
                ? ReservationStatus.APPROVED
                : ReservationStatus.PENDING;

        if(status.equals(ReservationStatus.APPROVED)){
            System.out.println("Called CleaningService");
            sendReservationPaidEvent(reservationEntity, response);
        }
        else{
            log.info("Payment failed or rejected for room {}: reservationId={}",
                    reservationEntity.getRoom().getId(), id);
        }

        reservationEntity.setReservationStatus(status);
        reservationEntity.setPaymentId(response.paymentId());
        reservationEntity.setPaymentStatus(response.paymentStatus());
        log.info("Payment processed for reservation {}. New status: {}", id, status);
        return repository.save(reservationEntity);
    }

    private void sendReservationPaidEvent(
            ReservationEntity reservationEntity,
            PaymentResponseDto response
    ) {
        log.info("Sending ReservationPaidEvent to Kafka for reservation {}", reservationEntity.getId());
        kafkaTemplate.send(
                reservationPaidTopic,
                reservationEntity.getId(),
                ReservationPaidEvent.builder()
                        .reservationId(reservationEntity.getId())
                        .roomId(reservationEntity.getRoom().getId())
                        .build()
        );
    }



    public void processCleaningAssigned(CleaningAssignedEvent cleaningAssignedEvent) {
        var reservation = getReservationOrThrow(cleaningAssignedEvent.reservationId());

        if(reservation.getPaymentStatus() != PaymentStatus.PAID){
            throw new IllegalStateException("Can't process cleaning assigned reservation");
        }
        reservation.setRoomStatus(cleaningAssignedEvent.status());
        reservation.setCleanerId(cleaningAssignedEvent.cleanerId());
        reservation.setEtaMinutes(cleaningAssignedEvent.etaMinutes());
        repository.save(reservation);
        log.info("Cleaning assigned and status updated for reservation {}",
                cleaningAssignedEvent.reservationId());
    }

    private ReservationEntity getReservationOrThrow(Long id){
        var reservationEntityOptional = repository.findById(id);
        return reservationEntityOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }
}