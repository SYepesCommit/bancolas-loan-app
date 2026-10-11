package com.bancolas.loan.service;

import com.bancolas.loan.constant.LogMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendLoanStatusEmail(String toAddress, String reference, String status, String reason, Double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toAddress);
        message.setSubject(String.format(LogMessages.EMAIL_SUBJECT_STATUS, reference));

        if ("APPROVED".equalsIgnoreCase(status)) {
            message.setText(String.format(
                LogMessages.EMAIL_BODY_APPROVED,
                reference, amount
            ));
        } else {
            message.setText(String.format(
                LogMessages.EMAIL_BODY_REJECTED,
                reference, reason
            ));
        }

        mailSender.send(message);
        log.info(LogMessages.EMAIL_SENT_SUCCESS, toAddress);
    }
}