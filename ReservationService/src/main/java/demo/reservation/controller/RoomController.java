package demo.reservation.controller;

import demo.reservation.model.RoomRequestDto;
import demo.reservation.model.RoomResponseDto;
import demo.reservation.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponseDto> createRoom(
            @RequestBody @Valid RoomRequestDto roomRequest,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        log.info("Called: createRoom");
        RoomResponseDto createdRoom = roomService.createRoom(roomRequest, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        log.info("Called: deleteRoom with id: {}", id);
        roomService.deleteRoom(id, authorizationHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RoomResponseDto>> getAllRooms() {
        log.info("Called: getAllRooms");
        List<RoomResponseDto> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
}
