package dev.profitsoft.internship.rebrov.block_five.repository;

import dev.profitsoft.internship.rebrov.block_five.model.Message;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends ElasticsearchRepository<Message, Long> {
}
