package demo.reservation.repository;

import demo.common.model.status.RoomStatus;
import demo.reservation.model.entity.ReservationEntity;
import demo.reservation.model.status.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Modifying
    @Query("update ReservationEntity r " +
            "set r.reservationStatus= :reservationStatus " +
            "where r.id= :id")
    void setStatus(@Param("id") Long id,
                   @Param("reservationStatus") ReservationStatus reservationStatus
    );

    @Query("SELECT r.id from ReservationEntity r " +
            "WHERE r.room.id = :roomId " +
            "AND r.reservationStatus = :status " +
            "AND :startDate < r.endDate " +
            "AND r.startDate < :endDate")
    List<Long> findConflictReservationIds(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status
    );

    @Query("SELECT r from ReservationEntity r " +
            "WHERE (:roomId IS NULL OR r.room.id = :roomId) " +
            "AND (:userId IS NULL OR r.userId = :userId)")
    List<ReservationEntity> searchAllByFilter(
            @Param ("roomId") Long roomId,
            @Param ("userId") Long userId,
            Pageable pageable
    );

    List<ReservationEntity> findAllByRoomStatus(RoomStatus roomStatus);
}
