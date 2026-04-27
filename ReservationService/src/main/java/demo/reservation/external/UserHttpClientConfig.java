package demo.reservation.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class UserHttpClientConfig {

    @Value("${user-service.base-url:http://localhost:8083}")
    private String userServiceBaseUrl;

    @Bean
    RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl(userServiceBaseUrl)
                .build();
    }

    @Bean
    UserHttpClient userHttpClient(RestClient userRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(userRestClient))
                .build()
                .createClient(UserHttpClient.class);
    }
}
