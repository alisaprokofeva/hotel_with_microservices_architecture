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
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        log.info("Called: getReservationById: id = "+id);
        return ResponseEntity.ok(reservationService.getReservationById(id, authorizationHeader));

    }

    @GetMapping()
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations(
            @RequestParam (name = "roomId", required = false) Long roomId,
            @RequestParam (name = "pageSize", required = false) Integer pageSize,
            @RequestParam (name = "pageNumber", required = false) Integer pageNumber,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        log.info("Called: getAllReservations");
        var filter = new SearchByFilterDto(
                roomId,
                pageSize,
                pageNumber
        );
        return ResponseEntity.ok(reservationService.searchAllByFilter(filter, authorizationHeader));
    }

    @PostMapping()
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestBody @Valid ReservationRequestDto reservationToCreate,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        log.info("Called: createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate, authorizationHeader));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid ReservationRequestDto reservationToUpdate,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        log.info("Called: updateReservation");
        return ResponseEntity.ok(reservationService.updateReservation(id, reservationToUpdate, authorizationHeader));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        log.info("Called: cancelReservation");
        reservationService.cancelReservation(id, authorizationHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ReservationResponseDto> approveReservation(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        log.info("Called: approveReservation");
        var reservation = reservationService.approveReservation(id, authorizationHeader);
        return ResponseEntity.ok(reservation);
    }



}
