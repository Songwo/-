package com.itheima.csstudent.service.impl;

import com.itheima.csstudent.dao.ComposeEnvRepository;
import com.itheima.csstudent.po.ComposeEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComposeEnvService {

    @Autowired
    private ComposeEnvRepository composeEnvRepository;

    public ComposeEnvironment findByUserId(String userId) {
        return composeEnvRepository.findByUserId(userId);
    }
}

