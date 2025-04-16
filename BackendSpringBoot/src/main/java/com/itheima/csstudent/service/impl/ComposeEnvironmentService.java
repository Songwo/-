package com.itheima.csstudent.service.impl;

import com.itheima.csstudent.dao.ComposeEnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComposeEnvironmentService {
    @Autowired
    private ComposeEnvironmentRepository composeEnvironmentRepository;
}
