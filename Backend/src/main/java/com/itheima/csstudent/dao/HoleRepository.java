package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.Hole;
import com.itheima.csstudent.po.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface HoleRepository extends MongoRepository<Hole,String> {
    List<Hole> findAllBy();
}
