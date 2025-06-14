package com.Interview.AiAgent.Repository;

import com.Interview.AiAgent.Models.InterviewSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InterviewRepository extends MongoRepository<InterviewSession, String> {
}
