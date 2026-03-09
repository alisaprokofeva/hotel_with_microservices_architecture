package demo.reservation.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class PaymentHttpClientConfig {

    @Value("http://localhost:8081")
    private String paymentServiceBaseUrl;

    @Bean
    RestClient paymentRestClient() {
        return RestClient.builder()
                .baseUrl(paymentServiceBaseUrl)
                .build();
    }

    @Bean
    PaymentHttpClient paymentHttpClient(RestClient paymentRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(paymentRestClient))
                .build().createClient(PaymentHttpClient.class);
    }

}
