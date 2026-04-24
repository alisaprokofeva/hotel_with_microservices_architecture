package demo.reservation.model.entity;

import demo.common.model.status.RoomStatus;
import demo.common.model.status.PaymentStatus;
import jakarta.persistence.*;
import demo.reservation.model.status.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "reservations")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id")
    private Long userId;
    @Column(name="payment_id")
    private Long paymentId;
    @Column(name="room_id", nullable = false)
    private Long roomId;
    @Column(name="start_date", nullable = false)
    private LocalDate startDate;
    @Column(name="end_date", nullable = false)
    private LocalDate endDate;
    @Column(name="amount")
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name="reservation_status")
    private ReservationStatus reservationStatus;
    //надо вынести в общее, пока добавила какую то dependency
    @Enumerated(EnumType.STRING)
    @Column(name="payment_status")
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    @Column(name="room_status")
    private RoomStatus roomStatus;
    @Column(name = "cleaner_id")
    private Long cleanerId;

}
