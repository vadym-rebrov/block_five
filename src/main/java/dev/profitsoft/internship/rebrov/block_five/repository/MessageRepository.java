package dev.profitsoft.internship.rebrov.block_five.repository;

import dev.profitsoft.internship.rebrov.block_five.model.Message;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageRepository extends ElasticsearchRepository<Message, Long> {
}
