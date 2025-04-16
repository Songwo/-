package com.itheima.csstudent.service.impl;

import com.itheima.csstudent.dao.RewardRepository;
import com.itheima.csstudent.po.Question;
import com.itheima.csstudent.po.Reward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RewardService {

    @Autowired
    private RewardRepository rewardRepository;

    public List<Reward> getReward() {
        return rewardRepository.findAll();
    }

    public Reward findById(String id) {
        if(rewardRepository.findById(id).isEmpty())
            return null;
        return rewardRepository.findById(id).get();
    }

    public Reward save(Reward reward) {
        return rewardRepository.save(reward);
    }


}
