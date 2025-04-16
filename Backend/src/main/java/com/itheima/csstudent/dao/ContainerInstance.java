package com.itheima.csstudent.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContainerInstance extends MongoRepository<ContainerInstance, String> {
}
