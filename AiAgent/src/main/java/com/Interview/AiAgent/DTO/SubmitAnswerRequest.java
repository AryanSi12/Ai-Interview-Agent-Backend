package com.Interview.AiAgent.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAnswerRequest {
    private String answer;
    private String resume;
}
