package dev.profitsoft.internship.rebrov.block_five.service;

import dev.profitsoft.internship.rebrov.block_five.model.Message;
import dev.profitsoft.internship.rebrov.block_five.model.MessageStatus;
import dev.profitsoft.internship.rebrov.block_five.repository.MessageRepository;
import dev.profitsoft.internship.rebrov.block_five.service.impl.JavaEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageConsumer {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private JavaEmailService emailService;

    @KafkaListener(topics = "email-sending-tasks", groupId = "email-worker")
    public void consumeEmailTask(Message message) {
        log.info("Received task to send email to: {}", message.getRecipientsEmail());
        message.setStatus(MessageStatus.RECEIVED);
        message.setErrorMessage(null);
        messageRepository.save(message);

        try {
            emailService.send(message);
            message.setStatus(MessageStatus.SENT);
            messageRepository.save(message);
            log.info("Email sent successfully to: {}", message.getRecipientsEmail());

        } catch (Exception e) {
            log.error("Failed to send email", e);
            message.setStatus(MessageStatus.FAILED);
            message.setErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
            messageRepository.save(message);
        }
    }
}
