package demo.payment.mapper;

import demo.payment.model.PaymentRequestDto;
import demo.payment.model.PaymentResponseDto;
import demo.payment.model.entity.PaymentEntity;
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
