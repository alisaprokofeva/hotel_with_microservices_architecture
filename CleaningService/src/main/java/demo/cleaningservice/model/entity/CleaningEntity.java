package demo.cleaningservice.model.entity;

import demo.cleaningservice.model.status.CleanerStatus;
import demo.common.model.status.RoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cleanings")
public class CleaningEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservationId")
    private Long reservationId;

    @Column(name = "roomId")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name="cleaner_status", nullable = false)
    private CleanerStatus cleanerStatus;

    @Column(name = "eta_minutes")
    private Long etaMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name="room_status", nullable = false)
    private RoomStatus roomStatus;
}
