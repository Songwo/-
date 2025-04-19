package com.bmgf.dao;

import com.bmgf.po.Hole;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface HoleRepository extends MongoRepository<Hole,String> {
    List<Hole> findAllBy();
}
