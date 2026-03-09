//убрать lesson1 из pom.xml (name)

package demo.payment.service;

import demo.payment.repository.PaymentRepository;
import demo.payment.mapper.PaymentMapper;
import demo.payment.model.PaymentRequestDto;
import demo.payment.model.PaymentResponseDto;
import demo.payment.model.status.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponseDto makePayment(PaymentRequestDto requestDto) {
        var found = paymentRepository.findByReservationId(requestDto.reservationId());
        if (found.isPresent()) {
            return paymentMapper.toResponseDto(found.get());
        }

        var paymentEntity = paymentMapper.toEntity(requestDto);

        //заглушка
        PaymentStatus paymentStatus;
        if(requestDto.amount().intValue() % 2 == 0){
            paymentStatus = PaymentStatus.PAYMENT_FAILED;
        }
        else{
            paymentStatus = PaymentStatus.PAID;
        }
        paymentEntity.setPaymentStatus(paymentStatus);

        var savedEntity = paymentRepository.save(paymentEntity);
        return paymentMapper.toResponseDto(savedEntity);
    }
}
