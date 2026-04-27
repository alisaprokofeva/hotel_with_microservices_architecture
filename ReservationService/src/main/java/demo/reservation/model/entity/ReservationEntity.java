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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;
    @Column(name="start_date", nullable = false)
    private LocalDate startDate;
    @Column(name="end_date", nullable = false)
    private LocalDate endDate;
    @Column(name="amount", precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name="reservation_status")
    private ReservationStatus reservationStatus;
    @Enumerated(EnumType.STRING)
    @Column(name="payment_status")
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    @Column(name="room_status")
    private RoomStatus roomStatus;
    @Column(name = "cleaner_id")
    private Long cleanerId;
    @Column(name = "eta_minutes")
    private Long etaMinutes;

}
