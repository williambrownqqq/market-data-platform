package com.market.data.platform.kafka;

import com.market.data.platform.dto.kafka.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    @Value("${app.kafka.topics.order-events}")
    private String orderEventsTopic;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void publishNewOrderEvent(OrderEvent event) {
        event.setEventType("NEW_ORDER");
        event.setTimestamp(LocalDateTime.now());
        send(ordersTopic, event);
    }

    public void publishOrderExecutionEvent(OrderEvent event) {
        event.setEventType("ORDER_EXECUTED");
        event.setTimestamp(LocalDateTime.now());
        send(orderEventsTopic, event);
    }

    public void publishOrderCancellationEvent(OrderEvent event) {
        event.setEventType("ORDER_CANCELLED");
        event.setTimestamp(LocalDateTime.now());
        send(orderEventsTopic, event);
    }

    public void publishOrderRejectionEvent(OrderEvent event) {
        event.setEventType("ORDER_REJECTED");
        event.setTimestamp(LocalDateTime.now());
        send(orderEventsTopic, event);
    }

    public void publishOrderStatusUpdateEvent(OrderEvent event) {
        // Caller is responsible for setting eventType before calling this
        event.setTimestamp(LocalDateTime.now());
        send(orderEventsTopic, event);
    }

    // -------------------------------------------------------------------------
    // Internal send — single place for error handling & logging
    // -------------------------------------------------------------------------

    private void send(String topic, OrderEvent event) {
        String key = String.valueOf(event.getOrderId());

        log.info("Publishing event type='{}' orderId={} to topic='{}'",
                event.getEventType(), event.getOrderId(), topic);

        CompletableFuture<SendResult<String, OrderEvent>> future =
                orderEventKafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published ✓ topic='{}' partition={} offset={} orderId={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getOrderId());
            } else {
                // Non-retriable failure after producer exhausted its retries.
                // At this point the event is lost — alert/dead-letter handling
                // should be wired here in production (e.g. persist to outbox table).
                log.error("Failed to publish event type='{}' orderId={} to topic='{}': {}",
                        event.getEventType(), event.getOrderId(), topic, ex.getMessage(), ex);
            }
        });
    }
}