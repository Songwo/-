package com.bmgf.service.impl;

import com.bmgf.DTO.ChatMessageDTO;
import com.bmgf.DTO.ChatRoomDTO;
import com.bmgf.DTO.RoomDTO;
import com.bmgf.Manger.ChatRoomManager;
import com.bmgf.dao.ChatMessageRepository;
import com.bmgf.dao.ChatRoomRepository;
import com.bmgf.dao.UserRepository;
import com.bmgf.po.ChatMessage;
import com.bmgf.po.ChatRoom;
import com.bmgf.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository repository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    public ChatRoom createRoom(ChatRoom room) {
        room.setParticipants(new ArrayList<>());
        return repository.save(room);
    }

    public boolean joinRoom(String roomId, String username, String password) {
        Optional<ChatRoom> optRoom = repository.findById(roomId);
        if (optRoom.isEmpty()) return false;
        ChatRoom room = optRoom.get();
        if (!room.getPassword().equals(password)) return false;
        if (room.getExpireAt().isBefore(LocalDateTime.now())) return false;
        room.getParticipants().add(username);
        repository.save(room);
        return true;
    }

    public Optional<ChatRoom> getRoom(String id) {
        return repository.findById(id);
    }

    public ChatRoom save(ChatRoom room) {
        return repository.save(room);
    }

    public void delete(String roomId) {
        repository.deleteById(roomId);
    }

    public List<ChatRoomDTO> getAllRooms() {
        List<ChatRoom> rooms = mongoTemplate.findAll(ChatRoom.class, "chatRoom");

        return rooms.stream().map(room -> {
            String roomId = room.getId();

            // 1. 获取在线人数
            int onlineCount = ChatRoomManager.getOnlineCount(roomId);

            // 2. 查询消息数量
            long messageCount = mongoTemplate.count(
                    Query.query(Criteria.where("roomId").is(roomId)),
                    "messages"
            );

            // 3. 封装 DTO
            return ChatRoomDTO.from(room, onlineCount, messageCount);
        }).collect(Collectors.toList());
    }
    //根据分类返回房间数目
    public long countRoomsByCategory(String category) {
        return repository.countByCategory(category);
    }

    public long countMessagesForRoom(String roomId) {
        return mongoTemplate.count(Query.query(Criteria.where("roomId").is(roomId)), "chat_message");
    }

    // 所有分类统计
    public List<Map<String, Object>> countRoomsByAllCategories() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("category").count().as("count")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "chatRoom", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", (String) doc.get("_id"));
                    map.put("count", ((Number) doc.get("count")).intValue());
                    return map;
                })
                .toList();
    }

    // 转为 DTO
    public RoomDTO convertToDto(ChatRoom room) {
        long messageCount = countMessagesForRoom(room.getId());
        int onlineCount =  ChatRoomManager.getOnlineCount(room.getId());
        boolean hasPassword = room.getPassword() != null && !room.getPassword().isEmpty();
        return new RoomDTO(
                room.getId(), room.getTitle(), room.getCategory(),
                hasPassword, onlineCount, messageCount
        );
    }

    public List<RoomDTO> getAllRoomDTOsByCategory(String category) {
        List<ChatRoom> rooms = repository.findByCategory(category);
        return rooms.stream().map(this::convertToDto).toList();
    }

    /**
     * 查询某房间最近 N 条历史消息，并组装 DTO
     */
//    public List<ChatMessageDTO> getHistory(String roomId, int limit) {
//        List<ChatMessage> history = repository
//                .findTop50ByRoomIdOrderByTimestampDesc(roomId, limit);
//        // 正序
//        Collections.reverse(history);
//
//        return history.stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }

    /** 把单条消息转成带用户头像、昵称的 DTO */
    public ChatMessageDTO toDto(ChatMessage msg) {
        User u = userRepository.findById(msg.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ChatMessageDTO(
                msg.getId(),
                msg.getRoomId(),
                msg.getSenderId(),
                u.getUsername(),
                u.getAvatar(),
                msg.getContent(),
                msg.getTimestamp()
        );
    }

    /** 保存新消息 */
    public ChatMessageDTO saveAndConvert(ChatMessage msg) {
        ChatMessage saved = chatMessageRepository.save(msg);
        return toDto(saved);
    }

}

