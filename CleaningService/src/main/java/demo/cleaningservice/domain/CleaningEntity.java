package demo.cleaningservice.domain;

import demo.common.model.status.CleaningStatus;
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

    @Column(name = "reservationId", nullable = false)
    private Long reservationId;

    @Column(name = "roomId", nullable = false)
    private Long roomId;

    @Column(name = "cleanerId", nullable = false)
    private Long cleanerId;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private CleaningStatus status;
}
