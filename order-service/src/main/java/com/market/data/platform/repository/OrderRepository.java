package com.market.data.platform.repository;

import com.market.data.platform.model.Order;
import com.market.data.platform.model.OrderStatus;
import com.market.data.platform.model.OrderType;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status IN :statuses")
    List<Order> findByUserIdAndStatuses(@Param("userId") Long userId,
                                        @Param("statuses") List<OrderStatus> statuses);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId,
                          @Param("status") OrderStatus status);

    /**
     * Find all orders by user ID
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find all orders by user ID and status
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Find all orders by symbol
     */
    List<Order> findBySymbol(String symbol);

    /**
     * Find all orders by symbol and order type
     */
    List<Order> findBySymbolAndOrderType(String symbol, OrderType orderType);

    /**
     * Find all orders by status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by user ID ordered by creation date descending
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find orders created within a date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find pending orders for a user
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status IN ('NEW', 'PENDING')")
    List<Order> findPendingOrdersByUserId(@Param("userId") Long userId);

    /**
     * Count orders by user ID and status
     */
    Long countByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Check if user has any pending orders
     */
    boolean existsByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses);

    /**
     * Find the most recent order by user ID
     */
    Optional<Order> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find orders by multiple statuses
     */
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}

