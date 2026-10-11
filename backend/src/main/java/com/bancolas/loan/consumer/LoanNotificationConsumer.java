package com.bancolas.loan.consumer;

import com.bancolas.config.RabbitMQConfig;
import com.bancolas.loan.constant.LogMessages;
import com.bancolas.loan.event.LoanEvaluatedEvent;
import com.bancolas.loan.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanNotificationConsumer {

    private final EmailService emailService;
    
    @Value("${app.mail.test-recipient:usr-10@correo.com}")
    private String testRecipientEmail;


    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeForEmailNotification(LoanEvaluatedEvent event) {
        log.info(LogMessages.RABBIT_PROCESSING_EMAIL, event.requestReference());

        try {
            emailService.sendLoanStatusEmail(
                    testRecipientEmail,
                    event.requestReference(),
                    event.status(),
                    event.rejectionReason(),
                    event.amount() != null ? event.amount().doubleValue() : 0.0
            );
        } catch (Exception e) {
            log.error(LogMessages.RABBIT_EMAIL_ERROR, event.requestReference(), e.getMessage());
        }
    }
}