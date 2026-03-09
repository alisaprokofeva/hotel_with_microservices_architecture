package demo.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentEntity toEntity(PaymentRequestDto paymentRequestDto) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setAmount(paymentRequestDto.amount());
        paymentEntity.setReservationId(paymentRequestDto.reservationId());
        return paymentEntity;
    }

    public PaymentResponseDto toResponseDto(PaymentEntity paymentEntity){
        return new PaymentResponseDto(
                paymentEntity.getId(),
                paymentEntity.getReservationId(),
                paymentEntity.getPaymentStatus(),
                paymentEntity.getAmount()
        );
    }
}
