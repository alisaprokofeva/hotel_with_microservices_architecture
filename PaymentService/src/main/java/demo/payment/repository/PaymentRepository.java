package demo.payment.repository;

import demo.payment.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository <PaymentEntity,Long> {
    Optional<PaymentEntity> findByReservationId(long reservationId);
}
