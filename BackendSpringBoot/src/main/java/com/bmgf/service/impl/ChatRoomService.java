package com.bmgf.service.impl;
import com.bmgf.DTO.CreateRoomRequest;
import com.bmgf.dao.ChatRoomRepository;
import com.bmgf.dao.UserRepository;
import com.bmgf.exception.RoomNotFoundException;
import com.bmgf.exception.UnauthorizedRoomOperationException;
import com.bmgf.exception.UserNotFoundException;
import com.bmgf.po.ChatRoom;
import com.bmgf.po.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public ChatRoom createRoom(CreateRoomRequest request, String creatorId) {
        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException(creatorId));

        ChatRoom room = ChatRoom.builder()
                .name(request.name())
                .category(request.category())
                .password(passwordEncoder.encode(request.password()))
                .creatorId(creatorId)
                .createdAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusDays(7)) // 默认7天有效期
                .build();

        creator.getOwnedRooms().add(room.getId());
        userRepo.save(creator);
        return chatRoomRepo.save(room);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void cleanExpiredRooms() {
        chatRoomRepo.deleteByExpireAtBefore(LocalDateTime.now());
    }

    public void deleteRoom(String roomId, String userId) {
        ChatRoom room = chatRoomRepo.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (!room.getCreatorId().equals(userId)) {
            throw new UnauthorizedRoomOperationException("删除房间");
        }

        chatRoomRepo.delete(room);
    }
    // 添加密码验证方法
    public ChatRoom validatePassword(String roomId, String password) {
        ChatRoom room = chatRoomRepo.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (!passwordEncoder.matches(password, room.getPassword())) {
            throw new UnauthorizedRoomOperationException("密码验证失败");
        }

        return room;
    }
    public ChatRoom saveRoom(ChatRoom room) {
        return chatRoomRepo.save(room);
    }
}
