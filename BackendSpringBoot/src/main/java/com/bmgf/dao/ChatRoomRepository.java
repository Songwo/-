package com.bmgf.dao;
import com.bmgf.po.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    // 基础CRUD操作已由MongoRepository提供

    // 1. 按创建者ID查找聊天室（自动生成查询）
    List<ChatRoom> findByCreatorId(String creatorId);

    // 2. 检查同名房间是否存在（组合查询）
    boolean existsByNameAndCreatorId(String name, String creatorId);

    // 3. 清理过期房间（自动生成删除操作）
    void deleteByExpireAtBefore(LocalDateTime expireAt);

    // 4. 查找用户加入的所有房间（集合包含查询）
    List<ChatRoom> findByMembersContaining(String userId);

    // 5. 按名称精确查找（单记录查询）
    Optional<ChatRoom> findByName(String name);

    // 6. 复杂查询示例：使用@Query注解
    @Query("{'$and': ["
            + "{ 'name': { '$regex': ?0, '$options': 'i' } }, "
            + "{ 'members': { '$size': { '$gt': ?1 } } }, "
            + "{ 'expireAt': { '$gt': ?2 } }"
            + "]}")
    List<ChatRoom> searchActiveRooms(String namePattern, int minMembers, LocalDateTime currentTime);
}
