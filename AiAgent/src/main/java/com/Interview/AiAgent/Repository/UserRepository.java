package com.Interview.AiAgent.Repository;

import com.Interview.AiAgent.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByusername(String username);
}
