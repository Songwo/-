package com.itheima.csstudent.service.impl;

import com.itheima.csstudent.dao.video_CategoryRepository;
import com.itheima.csstudent.po.Hole;
import com.itheima.csstudent.po.Video_Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoCategoryService {

    @Autowired
    private video_CategoryRepository repo;

    public Video_Category findById(String id) {
        if(repo.findById(id).isEmpty()){
            return null;
        };
        return repo.findById(id).get();
    }

    public void save(Video_Category vid){
        repo.save(vid);
    }

}
