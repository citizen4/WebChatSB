package kc87.repository.mongo;

import kc87.domain.ChatLog;
import org.springframework.data.mongodb.repository.MongoRepository;

@SuppressWarnings("unused")
public interface ChatLogRepository extends MongoRepository<ChatLog,String> {
}
