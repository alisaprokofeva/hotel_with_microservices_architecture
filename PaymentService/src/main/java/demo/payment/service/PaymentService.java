//убрать lesson1 из pom.xml (name)

package demo.payment.service;

import demo.common.model.dto.PaymentRequestDto;
import demo.common.model.dto.PaymentResponseDto;
import demo.common.model.status.PaymentStatus;
import demo.payment.repository.PaymentRepository;
import demo.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final Logger log =  LoggerFactory.getLogger(PaymentService.class);

    public PaymentResponseDto makePayment(PaymentRequestDto requestDto) {
        log.info("Called makePayment");
//        var found = paymentRepository.findByReservationId(requestDto.reservationId());
//        if (found.isPresent()) {
//            log.info("Found payment for reservation id {}", found.get().getReservationId());
//            return paymentMapper.toResponseDto(found.get());
//        }

        var paymentEntity = paymentMapper.toEntity(requestDto);

        //заглушка
        PaymentStatus paymentStatus;
        if(requestDto.amount().intValue() % 2 == 0){
            paymentStatus = PaymentStatus.PAYMENT_FAILED;
            log.info("Payment failed");

        }
        else{
            paymentStatus = PaymentStatus.PAID;
            log.info("Payment successful");
        }
        paymentEntity.setPaymentStatus(paymentStatus);

        var savedEntity = paymentRepository.save(paymentEntity);
        return paymentMapper.toResponseDto(savedEntity);
    }
}
