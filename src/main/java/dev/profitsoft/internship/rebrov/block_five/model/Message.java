package dev.profitsoft.internship.rebrov.block_five.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "messages")
public class Message {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    @NotBlank(message = "Sender email cannot be empty")
    @Email(message = "Invalid sender email format")
    private String senderEmail;

    @Field(type = FieldType.Keyword)
    @NotEmpty(message = "Recipient list cannot be empty")
    private List<@Email String> recipientEmails;

    @Field(type = FieldType.Text, analyzer = "standard")
    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @Field(type = FieldType.Text, analyzer = "standard")
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @Field(type = FieldType.Nested)
    private List<StatusHistory> history = new LinkedList<>();

    @Field(type = FieldType.Keyword)
    private MessageStatus currentStatus;

    @Field(type = FieldType.Text)
    private String currentErrorMessage;

    @Field(type = FieldType.Integer)
    @Builder.Default
    private Integer sentAttempt = 0;

    public void addStatus(MessageStatus newStatus, String details) {
        this.currentStatus = newStatus;
        this.currentErrorMessage = details;
        this.history.add(new StatusHistory(newStatus, Instant.now(), details));
    }
}