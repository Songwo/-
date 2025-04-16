package com.itheima.csstudent.service.impl;

import com.itheima.csstudent.dao.LabFlagRepository;
import com.itheima.csstudent.po.LabFlag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class LabFlagService {

    @Autowired
    private LabFlagRepository labFlagRepository;
    public boolean checkFlag(String imageName , String flag) {
    if(labFlagRepository.findByImageName(imageName).isEmpty()){
        return false;
    }else {
        LabFlag flag1 =labFlagRepository.findByImageName(imageName).get();
        if(flag1.getFlag().equals(flag)){
            return true;
        }else {
            return false;
        }
    }
    }

}
