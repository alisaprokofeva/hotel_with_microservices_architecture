package demo.reservation.service;

import demo.payment.model.status.PaymentStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository repository;
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(ReservationRepository repository, ReservationMapper reservationMapper, ReservationAvailabilityService availabilityService){
        this.repository = repository;
        this.reservationMapper = reservationMapper;
        this.availabilityService = availabilityService;
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
        reservationEntity.setReservationStatus(ReservationStatus.APPROVED);
        reservationEntity.setPaymentStatus(PaymentStatus.PENDING_PAYMENT);
        var approvedReservation = repository.save(reservationEntity);
        return reservationMapper.toResponseDto(approvedReservation);
    }
}
