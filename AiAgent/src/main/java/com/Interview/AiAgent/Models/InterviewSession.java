package com.Interview.AiAgent.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "interview_sessions")
public class InterviewSession {
    @Id
    private String sessionId;

    private String userId;

    private String domain;
    private String experienceLevel;

    private List<QA> qaList = new ArrayList<>();


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int totalQuestions; // New field

    private String resume;
    // Optional
    private String feedback;
    private Double score;

    @Data
    public static class QA {
        private String question;
        private String answer;
        private Double confidenceScore;
        private Double qualityScore;
        private Double communicationScore;
        private Double depthScore;
        private Double averageScore;


        private String scoreFeedbackText;
    }
}
