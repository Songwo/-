package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
