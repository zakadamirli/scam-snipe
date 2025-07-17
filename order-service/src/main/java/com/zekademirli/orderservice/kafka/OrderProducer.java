package com.zekademirli.orderservice.kafka;

import com.zekademirli.orderservice.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderRequest> kafkaTemplate;

    @Value("${order.topic}")
    private String topic;

    public void sendOrder(OrderRequest orderRequest) {
        kafkaTemplate.send(topic, orderRequest.getOrderId(), orderRequest);
    }
}
