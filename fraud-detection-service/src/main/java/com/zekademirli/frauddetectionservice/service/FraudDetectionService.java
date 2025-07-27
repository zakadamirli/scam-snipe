package com.zekademirli.frauddetectionservice.service;

import com.zekademirli.frauddetectionservice.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final BigDecimal SUSPICIOUS_AMOUNT_THRESHOLD = new BigDecimal("1000.00");

    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
            topics = "orders",
            groupId = "fraud-group",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void processOrder(
            @Payload(required = false) OrderRequest orderRequest,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("=== KAFKA MESSAGE RECEIVED ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        log.info("Raw OrderRequest: {}", orderRequest);

        if (orderRequest == null || isInvalidOrder(orderRequest)) {
            log.warn("❌ Invalid or null order received.");
            return;
        }

        try {
            log.info("🔍 Starting fraud detection for order: {}", orderRequest);

            FraudResult fraudResult = performFraudDetection(orderRequest);

            if (fraudResult.isFraudulent()) {
                log.warn("🚨 Fraudulent order detected: {}", orderRequest.getOrderId());
                log.warn("🚨 Fraud reasons: {}", fraudResult.getReasons());

                // ➕ Send fraud alert message to Kafka topic
                String alertMessage = String.format("Fraud detected for order %s. Reasons: %s",
                        orderRequest.getOrderId(), fraudResult.getReasons());

                kafkaTemplate.send("fraud-notifications", alertMessage);
                log.info("📤 Sent fraud alert to 'fraud-notifications' topic.");
            } else {
                log.info("✅ Legitimate order approved: {}", orderRequest.getOrderId());
            }

        } catch (Exception e) {
            log.error("❌ Error processing order: {} from offset: {}", orderRequest, offset, e);
        }
    }

    private boolean isInvalidOrder(OrderRequest orderRequest) {
        return orderRequest.getOrderId() == null ||
                orderRequest.getUserId() == null ||
                orderRequest.getAmount() == null;
    }

    private FraudResult performFraudDetection(OrderRequest order) {
        FraudResult result = new FraudResult();

        Double amountValue = order.getAmount();
        BigDecimal amount = amountValue != null ? BigDecimal.valueOf(amountValue) : BigDecimal.ZERO;

        log.info("🔍 Simple fraud check: OrderID: {}, Amount: {}", order.getOrderId(), amount);

        if (amount.compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) > 0) {
            result.addFraudReason("HIGH_AMOUNT: Order amount " + amount +
                    " exceeds suspicious threshold " + SUSPICIOUS_AMOUNT_THRESHOLD);
        }

        return result;
    }

    private static class FraudResult {
        private boolean fraudulent = false;
        private java.util.List<String> reasons = new java.util.ArrayList<>();

        public void addFraudReason(String reason) {
            this.fraudulent = true;
            this.reasons.add(reason);
        }

        public boolean isFraudulent() {
            return fraudulent;
        }

        public java.util.List<String> getReasons() {
            return reasons;
        }
    }
}
