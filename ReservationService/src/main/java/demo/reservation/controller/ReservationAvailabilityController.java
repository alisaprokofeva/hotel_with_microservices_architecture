package demo.reservation.controller;

import jakarta.validation.Valid;
import demo.reservation.model.status.AvailabilityStatus;
import demo.reservation.model.AvailabilityRequestDto;
import demo.reservation.model.AvailabilityResponseDto;
import demo.reservation.service.ReservationAvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {
    private final ReservationAvailabilityService service;
    private final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(
                  @Valid AvailabilityRequestDto request
    ){
        log.info("Called method cheakAvailability: request = {}", request);
        boolean isAvailable = service.isReservationAvailable(request.roomId(),
                request.startDate(),
                request.endDate()
        );
        var message = isAvailable ? "Room available to reservation"
                : "Room not available to reservation";
        var status = isAvailable ? AvailabilityStatus.AVAILABLE :
                AvailabilityStatus.RESERVED;
        return ResponseEntity.status(HttpStatus.OK).body(new AvailabilityResponseDto(message, status));
    }
}
