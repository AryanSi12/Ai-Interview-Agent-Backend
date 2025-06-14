package com.Interview.AiAgent.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndInterviewResponse {
    private String sessionId;
    private double averageScore;
    private String feedback;
}
