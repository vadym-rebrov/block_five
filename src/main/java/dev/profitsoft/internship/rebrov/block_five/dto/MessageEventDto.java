package dev.profitsoft.internship.rebrov.block_five.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventDto {
    @NotBlank(message = "Request ID cannot be blank")
    @UUID(message = "Request ID must be a valid UUID format")
    private String requestId;

    @NotEmpty(message = "Recipient list cannot be empty")
    private List<@Email(message = "Invalid email format") @NotBlank String> recipientEmails;

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 255, message = "Subject length must be less than 255 characters")
    private String subject;

    @NotBlank(message = "Content cannot be blank")
    private String content;
}
