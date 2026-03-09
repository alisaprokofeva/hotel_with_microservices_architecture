//убрать lesson1 из pom.xml (name)

package demo.payment;

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

        var entity = paymentMapper.toEntity(requestDto);

        //заглушка
        PaymentStatus status;
        if(requestDto.amount().intValue() % 2 == 0){
            status = PaymentStatus.PAYMENT_FAILED;
        }
        else{
            status = PaymentStatus.PAID;
        }
        entity.setPaymentStatus(status);

        var savedEntity = paymentRepository.save(entity);
        return paymentMapper.toResponseDto(entity);
    }
}
