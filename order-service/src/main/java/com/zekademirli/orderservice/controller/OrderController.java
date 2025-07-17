package com.zekademirli.orderservice.controller;

import com.zekademirli.orderservice.dto.OrderRequest;
import com.zekademirli.orderservice.model.Order;
import com.zekademirli.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderProducer;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order createOrder = orderProducer.createOrder(request);
        return ResponseEntity.ok(createOrder);
    }
}
