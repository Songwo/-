package com.bmgf.controller;

import com.bmgf.dao.ChatMessageRepository;
import com.bmgf.po.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepo;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message) {
        // 群发
        messagingTemplate.convertAndSend("/topic/public", message);
        chatMessageRepo.save(message);
    }

    @MessageMapping("/chat.private.{to}")
    public void sendPrivateMessage(@DestinationVariable String to, ChatMessage message) {
        messagingTemplate.convertAndSend("/queue/" + to, message);
        chatMessageRepo.save(message);
    }
}

