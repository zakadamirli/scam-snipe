package com.zekademirli.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    @Value("${notification.admin-email}")
    private String adminEmail;

    @KafkaListener(topics = "fraud-notifications", groupId = "notification-group")
    public void handleFraudNotification(String message) {
        log.info("📬 Received fraud notification: {}", message);

        try {
            if (message.contains("Fraud detected")) {
                // Extract order ID from message
                String orderId = extractOrderId(message);
                sendFraudAlertEmail(orderId, message);
                log.info("✅ Fraud alert email sent successfully for order: {}", orderId);
            } else {
                // Handle other notifications
                sendGeneralNotification(message);
                log.info("✅ General notification email sent successfully");
            }
        } catch (Exception e) {
            log.error("❌ Failed to send email notification: {}", e.getMessage(), e);
        }
    }

    private void sendFraudAlertEmail(String orderId, String fraudMessage) {
        try {
            String subject = "🚨 FRAUD ALERT - Order " + orderId;
            String emailBody =
                    "FRAUD DETECTED!\n\n" +
                            "Order ID: " + orderId + "\n" +
                            "Details: " + fraudMessage + "\n\n" +
                            "Please investigate immediately.\n\n" +
                            "Best regards,\n" +
                            "Fraud Detection System";

            emailService.sendSimpleEmail(adminEmail, subject, emailBody);
            log.info("📧 Fraud alert email sent to: {}", adminEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send fraud alert email: {}", e.getMessage());
            throw e;
        }
    }

    private void sendGeneralNotification(String notificationMessage) {
        try {
            String subject = "📢 System Notification";
            String emailBody =
                    "System Notification:\n\n" +
                            notificationMessage + "\n\n" +
                            "Best regards,\n" +
                            "Notification System";

            emailService.sendSimpleEmail(adminEmail, subject, emailBody);
            log.info("📧 General notification email sent to: {}", adminEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send general notification email: {}", e.getMessage());
            throw e;
        }
    }

    // Public method for manual testing
    public void sendTestEmail() {
        try {
            emailService.sendSimpleEmail(
                    adminEmail,
                    "🧪 Test Email",
                    "This is a test email from the notification service."
            );
            log.info("✅ Test email sent successfully");
        } catch (Exception e) {
            log.error("❌ Test email failed: {}", e.getMessage(), e);
        }
    }

    private String extractOrderId(String message) {
        try {
            // Extract order ID from message like "Fraud detected for order ORD123456..."
            String[] parts = message.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if ("order".equals(parts[i]) && i + 1 < parts.length) {
                    return parts[i + 1].replaceAll("[^A-Za-z0-9]", "");
                }
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.warn("Could not extract order ID from message: {}", message);
            return "UNKNOWN";
        }
    }
}