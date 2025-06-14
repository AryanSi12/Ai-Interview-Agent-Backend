package com.Interview.AiAgent.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewResponse {
    private String sessionId;
    private String nextQuestion;
    private String scoreFeedback;
}
