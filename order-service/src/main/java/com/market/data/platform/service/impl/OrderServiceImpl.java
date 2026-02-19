package com.market.data.platform.service.impl;

import com.market.data.platform.dto.kafka.OrderEvent;
import com.market.data.platform.dto.request.OrderRequestDTO;
import com.market.data.platform.dto.response.OrderResponseDTO;
import com.market.data.platform.dto.response.UserResponseDTO;
import com.market.data.platform.exception.InvalidOrderException;
import com.market.data.platform.exception.OrderNotFoundException;
import com.market.data.platform.exception.UserNotFoundException;
import com.market.data.platform.kafka.OrderEventProducer;
import com.market.data.platform.kafka.UserServiceClient;
import com.market.data.platform.mapper.OrderMapper;
import com.market.data.platform.model.Order;
import com.market.data.platform.model.OrderStatus;
import com.market.data.platform.repository.OrderRepository;
import com.market.data.platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.market.data.platform.model.OrderStatus.EXECUTED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository    orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final UserServiceClient userServiceClient;

    // Statuses that are considered "terminal" — no further transitions allowed
    private static final Set<OrderStatus> TERMINAL_STATUSES = Set.of(
            OrderStatus.EXECUTED,
            OrderStatus.FILLED,
            OrderStatus.REJECTED,
            OrderStatus.CANCELLED
    );

    // Only orders in these statuses may be cancelled by a user
    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(
            OrderStatus.NEW,
            OrderStatus.PENDING
    );

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        log.info("Creating order for userId={} symbol={} type={} qty={}",
                request.getUserId(), request.getSymbol(),
                request.getOrderType(), request.getQuantity());

        // 1. Verify user exists in User Service
        UserResponseDTO user = userServiceClient.getUserById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        log.debug("User verified: id={} username={}", user.getId(), user.getUsername());

        // 2. Map request → entity and set initial status
        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.NEW);

        // 3. Persist
        Order saved = orderRepository.save(order);
        log.info("Order persisted with id={}", saved.getId());

        // 4. Build Kafka event and publish
        OrderEvent event = orderMapper.orderEventToEntity(saved);
        event.setEventType("NEW_ORDER");
        orderEventProducer.publishNewOrderEvent(event);

        return orderMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        log.debug("Fetching order id={}", id);
        Order order = findOrderOrThrow(id);
        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        log.debug("Fetching all orders for userId={}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.toEntityList(orders);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        log.debug("Fetching all orders");
        return orderMapper.toEntityList(orderRepository.findAll());
    }

    @Override
    public List<OrderResponseDTO> getOrdersBySymbol(String symbol) {
        log.debug("Fetching orders for symbol={}", symbol);
        return orderMapper.toEntityList(orderRepository.findBySymbol(symbol.toUpperCase()));
    }

    @Override
    public List<OrderResponseDTO> getPendingOrdersByUserId(Long userId) {
        log.debug("Fetching pending orders for userId={}", userId);
        return orderMapper.toEntityList(orderRepository.findPendingOrdersByUserId(userId));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

//    @Override
//    @Transactional
//    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {
//        log.info("Updating order id={} → status={}", id, newStatus);
//
//        Order order = findOrderOrThrow(id);
//
//        validateStatusTransition(order.getStatus(), newStatus);
//
//        OrderStatus previousStatus = order.getStatus();
//        order.setStatus(newStatus);
//        Order updated = orderRepository.save(order);
//
//        log.info("Order id={} status changed: {} → {}", id, previousStatus, newStatus);
//
//        // Publish the appropriate event
//        OrderEvent event = orderMapper.orderEventToEntity(updated);
//        routeStatusEvent(event, newStatus);
//
//        return orderMapper.toResponse(updated);
//    }

    // -------------------------------------------------------------------------
    // CANCEL
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Cancelling order id={}", id);

        Order order = findOrderOrThrow(id);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderException(
                    String.format("Order id=%d cannot be cancelled in status '%s'. " +
                            "Only NEW or PENDING orders may be cancelled.", id, order.getStatus())
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelled = orderRepository.save(order);

        log.info("Order id={} cancelled successfully", id);

        OrderEvent event = orderMapper.orderEventToEntity(cancelled);
        orderEventProducer.publishOrderCancellationEvent(event);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * Validates that a status transition is legal.
     *
     * Allowed transitions:
     *   NEW       → PENDING, REJECTED, CANCELLED
     *   PENDING   → EXECUTED, REJECTED, CANCELLED
     *   EXECUTED  → FILLED
     *   FILLED    → (terminal)
     *   REJECTED  → (terminal)
     *   CANCELLED → (terminal)
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (TERMINAL_STATUSES.contains(current) && current != OrderStatus.EXECUTED) {
            throw new InvalidOrderException(
                    String.format("Order in status '%s' is terminal and cannot be updated.", current)
            );
        }

        boolean valid = switch (current) {
            case NEW      -> Set.of(OrderStatus.PENDING, OrderStatus.REJECTED, OrderStatus.CANCELLED).contains(next);
            case PENDING  -> Set.of(OrderStatus.EXECUTED, OrderStatus.REJECTED, OrderStatus.CANCELLED).contains(next);
            case EXECUTED -> next == OrderStatus.FILLED;
            default       -> false;
        };

        if (!valid) {
            throw new InvalidOrderException(
                    String.format("Illegal status transition: '%s' → '%s'", current, next)
            );
        }
    }

    /**
     * Routes a status-change event to the correct producer method.
     */
    private void routeStatusEvent(OrderEvent event, OrderStatus newStatus) {
        switch (newStatus) {
            case EXECUTED -> orderEventProducer.publishOrderExecutionEvent(event);
            case CANCELLED -> orderEventProducer.publishOrderCancellationEvent(event);
            case REJECTED  -> orderEventProducer.publishOrderRejectionEvent(event);
            default        -> orderEventProducer.publishOrderStatusUpdateEvent(event);
        }
    }
}