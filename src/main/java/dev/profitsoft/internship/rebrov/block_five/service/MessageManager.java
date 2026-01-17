package dev.profitsoft.internship.rebrov.block_five.service;

import dev.profitsoft.internship.rebrov.block_five.dto.MessageEventDto;
import dev.profitsoft.internship.rebrov.block_five.data.Message;
import dev.profitsoft.internship.rebrov.block_five.data.MessageStatus;
import dev.profitsoft.internship.rebrov.block_five.repository.MessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageManager {

    private final MessageRepository messageRepository;
    private final EmailService emailService;

    @Value("${scheduler.max-attempts.value:5}")
    private Integer maxAttempts;

    private static final int BATCH_SIZE = 100;

    @KafkaListener(topics = "movie-created-events", groupId = "email-worker")
    public void consumeEmailTask(@Payload @Valid MessageEventDto event) {
        log.info("Received event for request: {}", event.getRequestId());
        Message message = mapDtoToMessage(event);
        messageRepository.save(message);
        trySendEmail(message);
        messageRepository.save(message);
    }

    @Scheduled(fixedDelayString = "${scheduler.retry.interval}")
    public void retryFailedMessages() {
        log.info("Starting retry job...");
        List<Message> batchBuffer = new ArrayList<>(BATCH_SIZE);

        try (Stream<Message> messageStream = messageRepository.findByCurrentStatusAndSendingAttemptLessThan(
                MessageStatus.FAILED,
                maxAttempts
        )) {
            messageStream.forEach(message -> {
                trySendEmail(message);
                batchBuffer.add(message);

                if (batchBuffer.size() >= BATCH_SIZE) {
                    messageRepository.saveAll(batchBuffer);
                    batchBuffer.clear();
                }
            });

            if (!batchBuffer.isEmpty()) {
                messageRepository.saveAll(batchBuffer);
            }

        } catch (Exception e) {
            log.error("Error during retry job", e);
        }
    }

    private void trySendEmail(Message message) {
        try {
            emailService.send(message);
            message.addStatus(MessageStatus.SENT);
            log.info("Email sent for message id: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send email for id {}: {}", message.getId(), e.getMessage());
            String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            int newAttempt = (message.getSendingAttempt() == null ? 0 : message.getSendingAttempt()) + 1;
            message.setSendingAttempt(newAttempt);
            message.addStatus(MessageStatus.FAILED, errorMessage);
        }
    }

    private Message mapDtoToMessage(MessageEventDto event) {
        Message message = new Message();
        message.setId(event.getRequestId() != null ? event.getRequestId() : UUID.randomUUID().toString());
        message.setRecipientEmails(event.getRecipientEmails());
        message.setSubject(event.getSubject());
        message.setContent(event.getContent());
        message.addStatus(MessageStatus.RECEIVED);
        return message;
    }
}