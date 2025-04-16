package com.itheima.csstudent.service.impl;
import com.itheima.csstudent.dao.HoleRepository;
import com.itheima.csstudent.po.Hole;
import com.itheima.csstudent.po.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class HoleService {
@Autowired
    private HoleRepository holeRepository;
    public List<Hole> findHole(){
        return holeRepository.findAllBy();
    }
    public void save(Hole hole){
        holeRepository.save(hole);
    }
    public void delete(String id){
        holeRepository.deleteById(id);
    }
    public Hole findById(String id) {
        if(holeRepository.findById(id).isEmpty()){
            return null;
        };
        return holeRepository.findById(id).get();
    }

}
