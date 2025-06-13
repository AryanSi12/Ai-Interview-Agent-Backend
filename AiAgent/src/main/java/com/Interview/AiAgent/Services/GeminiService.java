// GeminiService.java
package com.Interview.AiAgent.Services;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GeminiService {

    private final GeminiClient geminiClient = new GeminiClient();

    public String getNextQuestion(String context, String resume, String lastAnswer,
                                  int questionNum, String experienceLevel, String questionType) {

        String prompt = """
            You are an intelligent and adaptive AI interviewer.

            Candidate Experience Level: %s

            Context so far:
            %s

            Resume:
            %s

            Previous Answer:
            %s

            Task:
            Ask question %d of the interview. Question type: %s.

            Guidelines:
            - Start with a short, friendly reply to the candidate's last answer.
            - Then ask ONE question related to the specified category:
                - "CS_CORE": Choose from OOP, DBMS, Operating Systems, Computer Networks (simple conceptual questions).
                - "PROJECT": Ask about project architecture, design choices, technologies used, or challenges.
                - "TECH_STACK": Ask about languages/frameworks mentioned (like React, Java, MongoDB, etc.).
                - For project you can ask questions on data structure , algorithms, or design patterns used.
            - Be **very lenient** for freshers, ask simple questions and keep language simple.
            - Ask only one clear, conceptual question.

            Output: Friendly transition + One interview question.
            """.formatted(experienceLevel, context, resume, lastAnswer, questionNum, questionType.toUpperCase());

        return geminiClient.generate(prompt);
    }

    public String getScore(String question, String answer, String experienceLevel) {
        String prompt = """
            You are an AI interview evaluator.

            Evaluate the following candidate's answer based on these criteria:
            - Confidence (out of 10)
            - Technical Quality (out of 10)
            - Communication (out of 10)
            - Depth (out of 10)

            Candidate Experience Level: %s

            Question: %s
            Answer: %s

            Instructions:
            - Be **very lenient for freshers**, especially on depth and terminology.
            - For freshers, mark high on depth if they show basic understanding.
            - Don't mark based on language fluency and informal words, rather focus on content.
            - Focus more on willingness to try, clarity, and basics for freshers.
            - Don't mark based on the length of the answer, rather focus on content.
            - For experienced candidates, apply stricter criteria.
            - Justify each score in 1 sentence.
            - Give final average score out of 10 at the end.

            Format strictly as:
            Confidence: X/10 - reason
            Quality: X/10 - reason
            Communication: X/10 - reason
            Depth: X/10 - reason
            Average Score: X/10
            """.formatted(experienceLevel, question, answer);

        return geminiClient.generate(prompt);
    }

    public boolean isInappropriateAnswer(String answer) {
        String prompt = """
            You are an AI assistant helping in a professional interview.

            Review the following candidate's answer and determine if it is **offensive, mocking, sarcastic, satirical, or highly unprofessional**. 

            Do **NOT** mark it inappropriate if the answer is vague, unclear, off-topic, or something like "I don't know".

            Text:
            "%s"

            Respond with exactly one word:
            - "OK" → if the response is acceptable, even if vague or unsure
            - "RUBBISH" → only if the response is rude, mocking, offensive, or clearly disrespectful
            """.formatted(answer);

        String result = geminiClient.generate(prompt).trim().toUpperCase();
        return result.contains("RUBBISH");
    }

    public String answerCandidateQuestion(String candidateQuestion) {
        String prompt = """
            You are an AI interviewer.

            A candidate has just completed the technical interview and asked this question:

            "%s"

            Give a short, clear, and professional answer as if you're an HR or technical interviewer.
            """.formatted(candidateQuestion);

        return geminiClient.generate(prompt);
    }
}
