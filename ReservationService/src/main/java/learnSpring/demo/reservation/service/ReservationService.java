package learnSpring.demo.reservation.service;

import jakarta.persistence.EntityNotFoundException;
import learnSpring.demo.reservation.model.Reservation;
import learnSpring.demo.reservation.model.entity.ReservationEntity;
import learnSpring.demo.reservation.mapper.ReservationMapper;
import learnSpring.demo.reservation.model.ReservationSearchFilter;
import learnSpring.demo.reservation.repository.ReservationRepository;
import learnSpring.demo.reservation.model.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.status()!=null){
            throw new IllegalArgumentException("Status should be empty");
        }
        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var entityToSave = reservationMapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        var savedEntity = repository.save(entityToSave);
        return reservationMapper.toDomain(savedEntity);
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id).orElseThrow(()->
                new EntityNotFoundException
                ("Not found reservation by id "+id));

        return reservationMapper.toDomain(reservationEntity);
    };


    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null ? filter.pageSize():10; //лучше вынести в applicationProperties
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber():0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream().map(reservationMapper::toDomain).toList();
    }

    public Reservation updateReservation
            (Long id,
             Reservation reservationToUpdate
    ) {
        var reservationEntity = repository.findById(id).orElseThrow(()-> new EntityNotFoundException
                ("Not found reservation by id"+id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Can't modify reservation: status = "+reservationEntity.getStatus());
        }
        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var reservationToSave = reservationMapper.toEntity((reservationToUpdate));
        reservationToSave.setStatus(ReservationStatus.PENDING);
        reservationToSave.setId(reservationEntity.getId());
        var updateReservation = repository.save(reservationToSave);
        return reservationMapper.toDomain(updateReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Not found reservation by id: "+id));
        if(reservation.getStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Can't cancel approved reservation. Contact" +
                    " with manager");
        }
        if(reservation.getStatus().equals(ReservationStatus.CANCELLED)){
            throw new IllegalArgumentException("Can't cancel the reservation." +
                    " Reservation was already cancelled");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    public Reservation approveReservation(Long id) {

        var reservationEntity = repository.findById(id).orElseThrow(()-> new EntityNotFoundException
                ("Not found reservation by id"+id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Can't approve reservation = "+reservationEntity.getStatus());
        }
        var isAvailableToApprove = availabilityService.isReservationAvailable(reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate());
        if(!isAvailableToApprove){
            throw new IllegalArgumentException("Can't approve reservation because of conflict");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        var approvedReservation = repository.save(reservationEntity);
        return reservationMapper.toDomain(approvedReservation);
    }
}
