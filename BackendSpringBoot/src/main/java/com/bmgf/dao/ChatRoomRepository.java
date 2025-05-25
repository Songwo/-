package com.bmgf.dao;

import com.bmgf.DTO.ChatRoomDTO;
import com.bmgf.po.ChatMessage;
import com.bmgf.po.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    List<ChatRoom> findByCategory(String category);

    long countByCategory(String category);

//    List<ChatMessage> findTop50ByRoomIdOrderByTimestampDesc(String roomId, int n);

}

