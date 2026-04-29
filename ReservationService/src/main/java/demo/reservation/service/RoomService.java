package demo.reservation.service;

import demo.reservation.external.UserHttpClient;
import demo.reservation.model.RoomRequestDto;
import demo.reservation.model.RoomResponseDto;
import demo.reservation.model.entity.RoomEntity;
import demo.reservation.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final UserHttpClient userHttpClient;

    public RoomResponseDto createRoom(RoomRequestDto roomRequest, String authorizationHeader) {
        ensureAdminAccess(authorizationHeader);

        RoomEntity roomEntity = new RoomEntity();
        roomEntity.setPrice(roomRequest.price());
        roomEntity.setImageUrls(roomRequest.imageUrls());

        RoomEntity saved = roomRepository.save(roomEntity);
        log.info("Room created with id: {}", saved.getId());
        return new RoomResponseDto(saved.getId(), saved.getPrice(), saved.getImageUrls());
    }

    public void deleteRoom(Long id, String authorizationHeader) {
        ensureAdminAccess(authorizationHeader);

        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found with id: " + id);
        }

        roomRepository.deleteById(id);
        log.info("Room deleted with id: {}", id);
    }

    public List<RoomResponseDto> getAllRooms() {
        List<RoomEntity> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(room -> new RoomResponseDto(room.getId(), room.getPrice(), room.getImageUrls()))
                .toList();
    }

    private void ensureAdminAccess(String authorizationHeader) {
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header must use Bearer token");
        }

        try {
            var currentUser = userHttpClient.getCurrentUser(authorizationHeader);
            if (currentUser == null || !"ADMIN".equals(currentUser.role())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin role required.");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token or access denied");
        }
    }
}
