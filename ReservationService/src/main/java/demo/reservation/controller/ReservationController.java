package demo.reservation.controller;

import demo.reservation.model.ReservationResponseDto;
import jakarta.validation.Valid;
import demo.reservation.model.ReservationRequestDto;
import demo.reservation.model.SearchByFilterDto;
import demo.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    @GetMapping("/{id}")
    //без RequestMapping надо было бы писать /reservation/{id}
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @PathVariable("id") Long id
    ){
        log.info("Called: getReservationById: id = "+id);
        return ResponseEntity.ok(reservationService.getReservationById(id));

    }

    @GetMapping()
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations(
            @RequestParam (name = "roomId", required = false) Long roomId,
            @RequestParam (name = "userId", required = false) Long userId,
            @RequestParam (name = "pageSize", required = false) Integer pageSize,
            @RequestParam (name = "pageNumber", required = false) Integer pageNumber
    ){
        log.info("Called: getAllReservations");
        var filter = new SearchByFilterDto(
                roomId,
                userId,
                pageSize,
                pageNumber
        );
        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));
    }

    @PostMapping()
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestBody @Valid ReservationRequestDto reservationToCreate
    ){
        log.info("Called: createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid ReservationRequestDto reservationToUpdate
    ){
        log.info("Called: updateReservation");
        return ResponseEntity.ok(reservationService.updateReservation(id, reservationToUpdate));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called: cancelReservation");
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ReservationResponseDto> approveReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called: approveReservation");
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }

}
