package com.bancolas.loan.consumer;

import com.bancolas.config.RabbitMQConfig;
import com.bancolas.loan.constant.LogMessages;
import com.bancolas.loan.event.LoanEvaluatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoanAuditConsumer {

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void consumeEvaluatedLoanEvent(LoanEvaluatedEvent event) {
        log.info(LogMessages.RABBIT_EVENT_RECEIVED,
                event.requestReference(),
                event.customerId(),
                event.status());

        if ("APPROVED".equalsIgnoreCase(event.status())) {
            log.info(LogMessages.RABBIT_APPROVED_SIMULATION, event.customerId());
        } else {
            log.warn(LogMessages.RABBIT_REJECTED_SIMULATION, event.rejectionReason());
        }
    }
}