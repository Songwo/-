package com.bmgf.dao;

import com.bmgf.po.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findTop50ByRoomIdOrderByTimestampDesc(String roomId);
    long countByRoomId(String roomId);
}
