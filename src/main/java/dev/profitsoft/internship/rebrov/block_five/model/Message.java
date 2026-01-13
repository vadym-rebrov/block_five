package dev.profitsoft.internship.rebrov.block_five.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    private List<@Email String> recipientsEmail;

    @Field(type = FieldType.Text, analyzer = "standard")
    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @Field(type = FieldType.Text, analyzer = "standard")
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @Field(type = FieldType.Nested)
    private List<Attachment> attachments;

    @Field(type = FieldType.Keyword)
    private MessageStatus status;
}