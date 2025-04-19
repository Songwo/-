package com.bmgf.controller;
import com.bmgf.dao.AnswerRecordRepository;
import com.bmgf.dao.CategoryRepository;
import com.bmgf.dao.QuestionRepository;
import com.bmgf.dao.UserRepository;
import com.bmgf.exception.ResourceNotFoundException;
import com.bmgf.po.*;
import com.bmgf.service.impl.VulnContainerService;
import com.bmgf.util.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api")
public class QuestionController {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnswerRecordRepository answerRecordRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private JwtUtil jwtUtil;
@GetMapping("/questions")
public Result getRandomQuestions(@RequestHeader("Authorization") String authHeader) {
    // 增强类型安全校验
    String token = authHeader.substring(7); // 去掉"Bearer "前缀
    String username = jwtUtil.getUsernameFromToken(token);
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    String userId =user.getId();
    // 获取用户已答过的题目ID
    User user1 = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    Set<String> completedQuestions = new HashSet<>(user.getCompletedQuestions());
    // 排除已答题目，随机抽取10道题
    List<Question> questions = questionRepository.findRandomQuestionsExcluding(completedQuestions, 10);
    return Result.success(questions);
}
    @GetMapping("/questions_category")
    public Result getRandomQuestionsCategory(@RequestParam String category) {
        List<Question>questions = questionRepository.findByCategory(category);
        if (questions.isEmpty()) {
            return Result.error("题库咱时没有该类别");
        }else {
            return Result.success(questions);
        }
    }

    @GetMapping("/get_challenge")
    public Result getRandomChallenge() {
        Category category = categoryRepository.findAll().get(0);
        return Result.success(category);
    }
    @PostMapping("/submit")
    public Result submitExam(@RequestBody ExamSubmissionRequest request,
                             @RequestHeader("Authorization") String authHeader) {
        // 增强类型安全校验
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        String userId =user.getId();
        // 2. 初始化响应结构
        List<QuestionResult> questionResults = new ArrayList<>();
        int totalScoreDelta = 0;
        Set<String> newCompletedQuestions = new HashSet<>();
        // 3. 遍历所有题目答案
        for (Map.Entry<String, String> entry : request.getAnswers().entrySet()) {
            String questionId = entry.getKey();
            String userAnswer = entry.getValue();
            // 3.1 检查是否重复答题
            if (user.getCompletedQuestions().contains(questionId)) {
                questionResults.add(new QuestionResult(questionId, false, 0, "重复提交",userAnswer));
                continue;
            }
            // 3.2 验证题目存在性
            Question question = questionRepository.findById(questionId)
                    .orElseGet(() -> {
                        questionResults.add(new QuestionResult(questionId, false, 0, "题目不存在",userAnswer));
                        return null;
                    });
            if (question == null) continue;
            // 3.3 校验答案
            boolean isCorrect = question.getAnswer().equalsIgnoreCase(userAnswer);
            int scoreDelta = isCorrect ? question.getScore() : 0;
            // 3.4 记录结果
            questionResults.add(new QuestionResult(
                    questionId,
                    isCorrect,
                    scoreDelta,
                    isCorrect ? "回答正确" : "正确答案: " + question.getAnswer(),
                    userAnswer // 新增参数
            ));
            // 3.5 更新统计
            totalScoreDelta += scoreDelta;
            newCompletedQuestions.add(questionId);
        }
        // 4. 原子更新用户数据
        if (!newCompletedQuestions.isEmpty()) {
            userRepository.updateUserStats(
                    userId,
                    totalScoreDelta,
                    (int) questionResults.stream().filter(QuestionResult::isCorrect).count(),
                    newCompletedQuestions
            );
        }
        // 5. 批量保存答题记录
        List<AnswerRecord> records = questionResults.stream()
                .filter(r -> !"重复提交".equals(r.getMessage()))
                .map(r -> new AnswerRecord(
                                null,//MongDB自动生成
                                userId,            // 对应 private String userId;
                                r.getQuestionId(), // 对应 private String questionId;
                                request.getExamId(), // 对应 private String examId;
                                r.getUserAnswer(),   // 对应 private String userAnswer;
                                r.isCorrect(),       // 对应 private boolean correct;
                                r.getScoreDelta(),// 对应 private int scoreDelta;
                                LocalDateTime.now()
                        )
                )
                .collect(Collectors.toList());
        answerRecordRepository.saveAll(records);
        // 6. 构建响应
        return Result.success(new ExamResult(
                totalScoreDelta,
                user.getTotalScore() + totalScoreDelta,
                questionResults
        ));
    }
    // 请求类
    @Data
    static class ExamSubmissionRequest {
        @NotBlank(message = "考试ID不能为空")
        private String examId;
        @NotEmpty(message = "至少需要一道题的答案")
        private Map<String, String> answers; // questionId -> userAnswer
    }
    // 响应结构
    @Data
    @AllArgsConstructor
    static class ExamResult {
        private int scoreDelta; // 本次得分变化
        private int totalScore; // 最新总分
        private List<QuestionResult> details; // 每题详情
    }
    @Data
    @AllArgsConstructor
    static class QuestionResult {
        private String questionId;
        private boolean correct;
        private int scoreDelta;
        private String message;
        private String userAnswer; // 新增字段
    }
}
