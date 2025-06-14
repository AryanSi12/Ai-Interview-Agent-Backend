package com.Interview.AiAgent.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartInterviewRequest {
    private String userId;
    private String resume;
    private String domain;
    private String experienceLevel;
    private int totalQuestions;
}
