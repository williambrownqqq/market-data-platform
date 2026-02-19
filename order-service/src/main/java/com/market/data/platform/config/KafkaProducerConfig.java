package com.market.data.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;
import com.market.data.platform.dto.kafka.OrderEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // -------------------------------------------------------------------------
    // Consumer factory
    // -------------------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,  bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Key
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Value — wrapped in ErrorHandlingDeserializer so malformed JSON goes to DLT
        // instead of poisoning the consumer
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES,    "com.trading.orderservice.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,  OrderEvent.class.getName());

        // Offset strategy
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,    "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,   false);   // manual ack

        // Throughput / reliability tuning
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,         100);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,     30_000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,  10_000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,  300_000);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    // -------------------------------------------------------------------------
    // Error handler — exponential back-off + Dead Letter Topic
    // -------------------------------------------------------------------------

    /**
     * On processing failure:
     *   attempt 1 → wait 1 s → attempt 2 → wait 2 s → attempt 3 → DLT
     *
     * The KafkaTemplate used here is the same producer template, so DLT messages
     * are written using the same serialization as normal events.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    log.error("Sending failed record to DLT. topic='{}' offset={} cause='{}'",
                            record.topic(), record.offset(), ex.getMessage());
                    // DLT topic name = original topic + "-dlt"  (matches KafkaTopicConfig)
                    return new org.apache.kafka.common.TopicPartition(
                            record.topic() + "-dlt", record.partition());
                });

        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxAttempts(3);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    // -------------------------------------------------------------------------
    // Listener container factory
    // -------------------------------------------------------------------------

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
    orderEventKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderEventConsumerFactory());
        factory.setConcurrency(3);                         // 3 threads per topic partition
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}