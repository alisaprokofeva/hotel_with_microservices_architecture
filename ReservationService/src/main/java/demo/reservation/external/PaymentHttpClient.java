package demo.reservation.external;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/pay"
)
public interface PaymentHttpClient {
    @PostExchange
    PaymentResponseDto createPayment(
            @RequestBody PaymentRequestDto requestDto
    );
}
