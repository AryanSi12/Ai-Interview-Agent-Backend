// GeminiService.java
package com.Interview.AiAgent.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GeminiService {

    @Autowired
    private GeminiClient geminiClient;

    public String getNextQuestion(String context, String resume, String lastAnswer, int questionNumber,
                                  String experienceLevel, String questionType, String domain) {

        String prompt = """
        You are an AI interviewer conducting a professional mock interview.

        Candidate's Resume:
        %s

        Candidate's Domain: %s

        Conversation so far:
        %s

        Last Answer:
        %s

        Instructions:
        - Start with a short, friendly reply to the candidate's last answer.
        - Then ask ONE interview question related to the category: %s
        - Try to **connect the question with the candidate's domain** (%s) if possible.
        - Make sure some questions are from the domain's perspective as well.
        - try not to ask very long questions

        Question Category Rules:
        - "CS_CORE": Choose from **OOP, DBMS, Operating Systems, or Computer Networks**.
            - Prefer questions that relate to the candidate's domain (e.g., DBMS for FULLSTACK, OS for DEVOPS).
            - Combine two topics where possible (e.g., OS + DBMS).
            - Ask only simple **conceptual** questions.
        - "PROJECT": Ask about architecture, design decisions, tech stack, or challenges faced in domain-related projects.
            - You may also ask about **data structures**, **algorithms**, or **design patterns** used.
        - "TECH_STACK": Ask about **specific technologies** relevant to their domain (e.g., React, Spring Boot, Docker, PyTorch).

        Tone & Complexity Guidelines:
        - Be **very lenient for freshers**, especially on depth and terminology.
        - Keep language **simple** and beginner-friendly.
        - Ask only **one clear, non-ambiguous, conceptual** question.
        - Avoid repeating previously asked questions.
        - Avoid overly complex syntax or deep theory unless experience level is senior.

        Respond with only your reply and the next question.
        """.formatted(resume, domain, context, lastAnswer, questionType, domain);

        return geminiClient.generate(prompt).trim();
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
            - Mark freshers well if they are close to the answer
            

            Format strictly as:
            Confidence: X/10 - reason
            Quality: X/10 - reason
            Communication: X/10 - reason
            Depth: X/10 - reason
            Average Score: X/10
            """.formatted(experienceLevel, question, answer);

        return geminiClient.generate(prompt);
    }

    public String extractResumeDetails(String resumeText) {
        String prompt = """
        You are an intelligent resume parser.

        Here is a candidate's resume:

        %s

        Extract and return the following details in JSON format:
        {
          "name": "...",
          "email": "...",
          "phone": "...",
          "skills": [...],
          "projects": [...],
          "experience": "...",
          "education": "..."
        }
        Be word and case sensitive
        fetch most of the important details below mentioned fields
        Only include skills and technologies that are actually mentioned in the resume.
        """.formatted(resumeText);

        return geminiClient.generate(prompt);
    }


    public boolean isInappropriateAnswer(String answer) {
        String prompt = """
        You are an AI assistant helping in a professional interview.

        Review the following candidate's answer and determine if it is **highly unprofessional** — meaning it contains clear signs of **rudeness, personal attacks, inappropriate language, or explicit mocking**.

        Be **lenient**. Do NOT mark it inappropriate if the answer is:
        - vague or unclear
        - off-topic
        - casual or a bit informal
        - shows hesitation like "I don't know" or "I'm not sure"
        - uses mild humor

        Only mark it inappropriate if it is **clearly**:
        - disrespectful
        - mocking the interviewer
        - using abusive language
        - highly sarcastic in a negative or hostile way

        Candidate Answer:
        "%s"

        Respond with exactly one word:
        - "OK" → if the response is acceptable, even if unsure or informal
        - "RUBBISH" → only if clearly offensive, mocking, or abusive
        """.formatted(answer);

        String result = geminiClient.generate(prompt).trim().toUpperCase();
        return result.contains("RUBBISH");
    }

    public String answerCandidateQuestion(String candidateLastReply) {
        String prompt = """
        You are an AI interviewer.

        The candidate has just completed the technical interview. Here's their final message:

        "%s"

        Please respond with a short, clear, and professional message as if you're the HR or technical interviewer.
        
        - Politely acknowledge their message.
        - Conclude the interview on a positive note.
        - Suggest 2–3 key areas they can focus on to improve.
        - Keep it encouraging, constructive, and concise.
        """.formatted(candidateLastReply);

        return geminiClient.generate(prompt).trim();
    }

}
