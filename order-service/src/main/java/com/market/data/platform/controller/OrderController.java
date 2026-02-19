package com.market.data.platform.controller;


import com.market.data.platform.dto.request.OrderRequestDTO;
import com.market.data.platform.dto.response.OrderResponseDTO;
import com.market.data.platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        return new ResponseEntity<>(orderService.createOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

//    @PutMapping("/{id}/status")
//    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
//            @PathVariable Long id,
//            @RequestParam String status) {
//        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/user/{userId}/status/{status}")
//    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserIdAndStatus(
//            @PathVariable Long userId,
//            @PathVariable String status) {
//        return ResponseEntity.ok(orderService.getOrdersByUserIdAndStatus(userId, status));
//    }

}
