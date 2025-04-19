package com.bmgf.dao;
import com.bmgf.po.User;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends MongoRepository<User,String> {
    @Query("{'username': ?0}")//?0表示参数的占位符
    Optional<User>  findByUsername(String username);
    @Query("{'id': ?0}")//使用注解功能自定MongoDB查询
    Optional<User> findByIdString(String id);
    @Query(value = "{}", sort = "{ 'totalScore' : -1 }")
    List<User> findTopUsersByScore(Pageable pageable);
    // UserRepository 添加方法
        @Query("{ '_id': ?0 }")
        @Update("{"
                + "$inc: { 'totalScore': ?1, 'correctCount': ?2 }, "
                + "$addToSet: { 'completedQuestions': { $each: ?3 } }"
                + "}")
        void updateUserStats(String userId, int scoreDelta, int correctDelta, Collection<String> questionIds);
    }


