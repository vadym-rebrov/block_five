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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest
@Testcontainers
public class MessageManagerIntegrationTest {
    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.2.4")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private KafkaTemplate<String, MessageEventDto> kafkaTemplate;

    @MockitoBean
    private EmailService emailService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save message with SENT status when email service succeeds")
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
    @DisplayName("Should save message with FAILED status when email service throws exception")
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

    @Test
    @DisplayName("Should not send email twice when duplicate message is received")
    void shouldNotSendEmailTwice_WhenDuplicateMessageArrives() throws InterruptedException {
        String sharedRequestId = UUID.randomUUID().toString();
        MessageEventDto event = MessageEventDto.builder()
                .requestId(sharedRequestId) // Один і той самий ID
                .recipientEmails(List.of("duplicate@test.com"))
                .subject("Idempotency Test")
                .content("Content")
                .build();

        Mockito.doNothing().when(emailService).send(any(Message.class));
        kafkaTemplate.send("movie-created-events", event);
        Thread.sleep(2000);
        kafkaTemplate.send("movie-created-events", event);
        Thread.sleep(2000);
        verify(emailService, times(1)).send(any(Message.class));
        Optional<Message> savedMessage = messageRepository.findById(sharedRequestId);
        assertThat(savedMessage).isPresent();
        assertThat(savedMessage.get().getSendingAttempt()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should ignore message when request ID is invalid UUID")
    void shouldIgnoreMessage_WhenDtoIsInvalid() throws InterruptedException {

        MessageEventDto invalidDto = MessageEventDto.builder()
                .requestId("invalid-uuid-string")
                .recipientEmails(List.of("valid@email.com"))
                .subject("Subject")
                .content("Content")
                .build();

        kafkaTemplate.send("movie-created-events", invalidDto);
        Thread.sleep(2000);
        verify(emailService, never()).send(any(Message.class));
        assertThat(messageRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should ignore message when recipient list is empty")
    void shouldIgnoreMessage_WhenRecipientListIsEmpty() throws InterruptedException {
        MessageEventDto invalidDto = MessageEventDto.builder()
                .requestId(UUID.randomUUID().toString())
                .recipientEmails(Collections.emptyList())
                .subject("Subject")
                .content("Content")
                .build();
        kafkaTemplate.send("movie-created-events", invalidDto);
        Thread.sleep(2000);
        verify(emailService, never()).send(any(Message.class));
        assertThat(messageRepository.count()).isEqualTo(0);
    }
}
