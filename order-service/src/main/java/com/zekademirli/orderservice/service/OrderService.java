package com.zekademirli.orderservice.service;

import com.zekademirli.orderservice.dto.OrderRequest;
import com.zekademirli.orderservice.enums.OrderStatus;
import com.zekademirli.orderservice.kafka.OrderProducer;
import com.zekademirli.orderservice.model.Order;
import com.zekademirli.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public Order createOrder(OrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        orderProducer.sendOrder(request);
        return savedOrder;
    }
}
