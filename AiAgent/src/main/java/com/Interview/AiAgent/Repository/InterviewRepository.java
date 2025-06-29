package com.Interview.AiAgent.Repository;

import com.Interview.AiAgent.Models.InterviewSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InterviewRepository extends MongoRepository<InterviewSession, String> {
    List<InterviewSession> findByUserId(String userId);
}
