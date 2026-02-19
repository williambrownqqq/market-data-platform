package com.market.data.platform.kafka;

import com.market.data.platform.dto.kafka.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    // -------------------------------------------------------------------------
    // Main listener — order-events topic
    // Retry: 3 attempts with exponential back-off, then routes to DLT
    // -------------------------------------------------------------------------

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics            = "${app.kafka.topics.order-events}",
            groupId           = "${spring.kafka.consumer.group-id}",
            containerFactory  = "orderEventKafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC)     String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int    partition,
            @Header(KafkaHeaders.OFFSET)             long   offset,
            Acknowledgment acknowledgment) {

        log.info("Received event type='{}' orderId={} topic='{}' partition={} offset={}",
                event.getEventType(), event.getOrderId(), topic, partition, offset);

        try {
            dispatch(event);
            acknowledgment.acknowledge();
            log.debug("Acknowledged offset={}", offset);
        } catch (Exception ex) {
            // Re-throw so @RetryableTopic can schedule retries.
            // After all attempts are exhausted the message lands on the DLT.
            log.error("Processing failed for orderId={}, will retry: {}", event.getOrderId(), ex.getMessage());
            throw ex;
        }
    }

    // -------------------------------------------------------------------------
    // Dead Letter Topic handler
    // -------------------------------------------------------------------------

    @DltHandler
    public void handleDlt(
            @Payload  OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET)         long   offset) {

        // In production: persist to a dead_letter_orders table and alert on-call
        log.error("DLT: Unprocessable event type='{}' orderId={} topic='{}' offset={}. " +
                        "Requires manual intervention.",
                event.getEventType(), event.getOrderId(), topic, offset);
    }

    // -------------------------------------------------------------------------
    // Audit / monitoring listener — all order topics, separate consumer group
    // -------------------------------------------------------------------------

    @KafkaListener(
            topics           = {"${app.kafka.topics.orders}", "${app.kafka.topics.order-events}"},
            groupId          = "order-audit-group",
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void auditAllEvents(
            @Payload  OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("AUDIT | topic='{}' eventType='{}' orderId={} userId={} symbol={} status={}",
                topic, event.getEventType(), event.getOrderId(),
                event.getUserId(), event.getSymbol(), event.getStatus());

        acknowledgment.acknowledge();
    }

    // -------------------------------------------------------------------------
    // Event dispatcher
    // -------------------------------------------------------------------------

    private void dispatch(OrderEvent event) {
        switch (event.getEventType()) {
            case "NEW_ORDER"      -> handleNewOrder(event);
            case "ORDER_EXECUTED" -> handleOrderExecuted(event);
            case "ORDER_CANCELLED"-> handleOrderCancelled(event);
            case "ORDER_REJECTED" -> handleOrderRejected(event);
            case "ORDER_FILLED"   -> handleOrderFilled(event);
            default               -> log.warn("Unknown eventType='{}' for orderId={}",
                    event.getEventType(), event.getOrderId());
        }
    }

    // -------------------------------------------------------------------------
    // Per-event handlers
    // Each method is intentionally kept small so it can be extracted into a
    // dedicated service class (NotificationService, AnalyticsService, etc.)
    // -------------------------------------------------------------------------

    private void handleNewOrder(OrderEvent event) {
        log.info("NEW_ORDER | orderId={} userId={} symbol={} {} qty={}",
                event.getOrderId(), event.getUserId(),
                event.getSymbol(), event.getOrderType(), event.getQuantity());

        // Hook points:
        // notificationService.notifyOrderReceived(event);
        // analyticsService.recordNewOrder(event);
    }

    private void handleOrderExecuted(OrderEvent event) {
        log.info("ORDER_EXECUTED | orderId={} userId={} symbol={} price={}",
                event.getOrderId(), event.getUserId(),
                event.getSymbol(), event.getPrice());

        // Hook points:
        // notificationService.notifyOrderExecuted(event);
        // portfolioService.updateHoldings(event);
    }

    private void handleOrderCancelled(OrderEvent event) {
        log.info("ORDER_CANCELLED | orderId={} userId={}",
                event.getOrderId(), event.getUserId());

        // Hook points:
        // notificationService.notifyOrderCancelled(event);
        // fundsService.releaseReservedFunds(event);
    }

    private void handleOrderRejected(OrderEvent event) {
        log.info("ORDER_REJECTED | orderId={} userId={}",
                event.getOrderId(), event.getUserId());

        // Hook points:
        // notificationService.notifyOrderRejected(event);
        // metricsService.incrementRejectionCounter(event.getSymbol());
    }

    private void handleOrderFilled(OrderEvent event) {
        log.info("ORDER_FILLED | orderId={} userId={} symbol={}",
                event.getOrderId(), event.getUserId(), event.getSymbol());

        // Hook points:
        // settlementService.settle(event);
        // portfolioService.finalizeHoldings(event);
    }
}
