package dev.profitsoft.internship.rebrov.block_five.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventDto {
    private String requestId;
    private List<String> recipientEmails;
    private String subject;
    private String content;
}
