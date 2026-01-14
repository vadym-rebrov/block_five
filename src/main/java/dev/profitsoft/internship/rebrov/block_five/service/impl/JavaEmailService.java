package dev.profitsoft.internship.rebrov.block_five.service.impl;

import dev.profitsoft.internship.rebrov.block_five.model.Message;
import dev.profitsoft.internship.rebrov.block_five.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class JavaEmailService implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String serverEmail;

    @Override
    public void send(Message message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(serverEmail);
        mailMessage.setReplyTo(message.getSenderEmail());

        if (message.getRecipientsEmail() != null && !message.getRecipientsEmail().isEmpty()) {
            String[] recipients = message.getRecipientsEmail().toArray(new String[0]);
            mailMessage.setTo(recipients);
        } else {
            throw new IllegalArgumentException("Recipient list cannot be empty");
        }

        mailMessage.setSubject(message.getSubject());
        mailMessage.setText(message.getContent());

        javaMailSender.send(mailMessage);
    }
}
