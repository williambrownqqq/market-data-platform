package com.market.data.platform.service;

import com.market.data.platform.dto.request.OrderRequestDTO;
import com.market.data.platform.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    public List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO createOrder(OrderRequestDTO request);

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    //OrderResponseDTO updateOrderStatus(Long id, String status);

    void cancelOrder(Long id);

    //List<OrderResponseDTO> getOrdersByUserIdAndStatus(Long userId, String status);

    List<OrderResponseDTO> getPendingOrdersByUserId(Long userId);

    List<OrderResponseDTO> getOrdersBySymbol(String symbol);
}
