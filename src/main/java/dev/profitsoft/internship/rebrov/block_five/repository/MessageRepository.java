package dev.profitsoft.internship.rebrov.block_five.repository;

import dev.profitsoft.internship.rebrov.block_five.data.Message;
import dev.profitsoft.internship.rebrov.block_five.data.MessageStatus;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface MessageRepository extends ElasticsearchRepository<Message, Long> {

    Stream<Message> findByCurrentStatusAndSendingAttemptLessThan(MessageStatus status, Integer maxAttempts);
}
