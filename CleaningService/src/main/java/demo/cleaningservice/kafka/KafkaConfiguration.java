package demo.cleaningservice.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProperties properties;

    @Bean
    public DefaultKafkaProducerFactory<Long, CleaningAssignedEvent> kafkaConsumerFactory(
            KafkaProperties properties) {
        Map<String, Object> producerProperties = properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    @Bean
    KafkaTemplate<Long, CleaningAssignedEvent> deliveryAssignedEventKafkaTemplate(
            DefaultKafkaProducerFactory<Long, CleaningAssignedEvent> deliveryAssignedEventProducerFactory
    ) {
        return new KafkaTemplate<>(deliveryAssignedEventProducerFactory);
    }

    @Bean
    public ConsumerFactory<Long, ReservationPaidEvent> reservationPaidEventConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "kafka");
        JacksonJsonDeserializer<ReservationPaidEvent> valueDeserializer =
                new JacksonJsonDeserializer<>(ReservationPaidEvent.class);
        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), valueDeserializer);
    }

    @Bean
    public KafkaListenerContainerFactory<?> reservationPaidEventListenerFactory(ConsumerFactory<Long, ReservationPaidEvent> reservationPaidEventConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, ReservationPaidEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reservationPaidEventConsumerFactory);
        factory.setBatchListener(false);
        return factory;
    }
}
