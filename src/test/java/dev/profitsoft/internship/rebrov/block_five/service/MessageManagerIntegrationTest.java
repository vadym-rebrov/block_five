package dev.profitsoft.internship.rebrov.block_five.service;

import dev.profitsoft.internship.rebrov.block_five.data.Message;
import dev.profitsoft.internship.rebrov.block_five.data.MessageStatus;
import dev.profitsoft.internship.rebrov.block_five.dto.MessageEventDto;
import dev.profitsoft.internship.rebrov.block_five.repository.MessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
@Testcontainers
public class MessageManagerIntegrationTest {
    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.2.4")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private MessageRepository messageRepository;

    @MockitoBean
    private EmailService emailService;

    @DynamicPropertySource
    static void setElasticProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
    }

    @Test
    @DisplayName("Успішний сценарій з реальним Elastic: повідомлення зберігається зі статусом SENT")
    void shouldSaveMessageWithSentStatus_WhenEmailServiceSucceeds() {
        String requestId = UUID.randomUUID().toString();
        MessageEventDto event = createEventDto(requestId);
        Mockito.doNothing().when(emailService).send(any(Message.class));
        messageManager.consumeEmailTask(event);
        verify(emailService, times(1)).send(any(Message.class));
        Optional<Message> savedMessageOpt = messageRepository.findById(requestId);
        assertThat(savedMessageOpt).isPresent();
        assertThat(savedMessageOpt.get().getCurrentStatus()).isEqualTo(MessageStatus.SENT);
    }

    @Test
    @DisplayName("Сценарій помилки з реальним Elastic: повідомлення зберігається зі статусом FAILED")
    void shouldSaveMessageWithFailedStatus_WhenEmailServiceThrowsException() {
        String requestId = UUID.randomUUID().toString();
        MessageEventDto event = createEventDto(requestId);
        String expectedError = "SMTP error";

        doThrow(new MailSendException(expectedError)).when(emailService).send(any(Message.class));
        messageManager.consumeEmailTask(event);

        Optional<Message> savedMessageOpt = messageRepository.findById(requestId);
        assertThat(savedMessageOpt).isPresent();

        Message savedMessage = savedMessageOpt.get();
        assertThat(savedMessage.getCurrentStatus()).isEqualTo(MessageStatus.FAILED);
        assertThat(savedMessage.getHistory().getLast().getDetails()).contains(expectedError);
    }

    private MessageEventDto createEventDto(String requestId) {
        return MessageEventDto.builder()
                .requestId(requestId)
                .recipientEmails(List.of("test@example.com"))
                .subject("Integration Test Subject")
                .content("Test Content")
                .build();
    }
}
