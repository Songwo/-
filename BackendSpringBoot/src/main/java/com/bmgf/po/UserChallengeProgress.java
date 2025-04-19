package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "user_challenge_progress")
public class UserChallengeProgress {

    @Id
    private String userId;
    private Set<String> unlockedChallenges = new HashSet<>();
    private List<CompletedChallenge> completedChallenges = new ArrayList<>();


}
