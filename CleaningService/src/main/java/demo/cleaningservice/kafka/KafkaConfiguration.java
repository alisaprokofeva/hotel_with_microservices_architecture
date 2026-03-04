package demo.cleaningservice.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConfiguration {

    @Bean
    public DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory() {}
}
