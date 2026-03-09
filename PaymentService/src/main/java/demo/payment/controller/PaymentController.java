package demo.payment.controller;

import demo.payment.model.PaymentRequestDto;
import demo.payment.model.PaymentResponseDto;
import demo.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponseDto createPayment(
            @RequestBody PaymentRequestDto requestDto
    ){
        log.info("Creating Payment");
        return paymentService.makePayment(requestDto);
    }
}
