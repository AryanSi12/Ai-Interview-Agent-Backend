package com.Interview.AiAgent.Services;

import com.Interview.AiAgent.DTO.EndInterviewResponse;
import com.Interview.AiAgent.DTO.InterviewResponse;
import com.Interview.AiAgent.DTO.StartInterviewRequest;
import com.Interview.AiAgent.DTO.SubmitAnswerRequest;
import com.Interview.AiAgent.Models.InterviewSession;
import com.Interview.AiAgent.Models.User;
import com.Interview.AiAgent.Repository.InterviewRepository;
import com.Interview.AiAgent.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InterviewService {
    @Autowired
    InterviewRepository interviewRepository;

    @Autowired
    GeminiService geminiService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;
    public InterviewResponse startInterview(StartInterviewRequest request) {
        try{
            String resumeText = request.getResume();

            InterviewSession session = new InterviewSession();
            session.setUserId(request.getUserId());
            session.setDomain(request.getDomain());
            session.setExperienceLevel(request.getExperienceLevel());
            session.setQaList(new ArrayList<>());
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            session.setTotalQuestions(request.getTotalQuestions() + 1);
            session.setResume(request.getResume());
            interviewRepository.save(session);

            String introQuestion = "Hello! I'm your AI interviewer. Before we dive into the interview, I’d love to get to know you a little better. Could you please start by introducing yourself and sharing what inspired you to choose this particular domain?";
            InterviewSession.QA firstQA = new InterviewSession.QA();
            firstQA.setQuestion(introQuestion);
            session.getQaList().add(firstQA);

            interviewRepository.save(session);

            return new InterviewResponse(session.getSessionId(), introQuestion,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InterviewResponse handleSubmit(String sessionId, SubmitAnswerRequest request) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<InterviewSession.QA> qaList = session.getQaList();
        int questionNum = qaList.size();

        // Set the answer to the current question
        InterviewSession.QA currQA = qaList.get(questionNum - 1);
        currQA.setAnswer(request.getAnswer());

        // Check for inappropriate content
        if (geminiService.isInappropriateAnswer(request.getAnswer())) {
            currQA.setScoreFeedbackText("Your response was flagged as inappropriate.");
            qaList.set(questionNum - 1, currQA); // update in list

            session.setUpdatedAt(LocalDateTime.now());
            interviewRepository.save(session);

            EndInterviewResponse endResponse = endInterview(sessionId);
            return new InterviewResponse(sessionId, null,
                    "Your response was flagged as inappropriate. The interview has been terminated.\n\n" +
                            "Final Score: " + endResponse.getAverageScore() + "\n" +
                            "Feedback: " + endResponse.getFeedback());
                    }


        // Score the answer using Gemini
        String scoreSummary = geminiService.getScore(
                currQA.getQuestion(),
                request.getAnswer(),
                session.getExperienceLevel()
        );

        currQA.setScoreFeedbackText(scoreSummary);
        System.out.println(scoreSummary);
        Map<String, Double> parsedScores = extractDetailedScores(scoreSummary);

        currQA.setConfidenceScore(parsedScores.getOrDefault("Confidence", null));
        currQA.setQualityScore(parsedScores.getOrDefault("Quality", null));
        currQA.setCommunicationScore(parsedScores.getOrDefault("Communication", null));
        currQA.setDepthScore(parsedScores.getOrDefault("Depth", null));
        currQA.setAverageScore(parsedScores.getOrDefault("Average", null));

        qaList.set(questionNum - 1, currQA);
        session.setQaList(qaList);
        session.setUpdatedAt(LocalDateTime.now());


        interviewRepository.save(session);
        // Build interview context so far
        StringBuilder context = new StringBuilder();
        for (InterviewSession.QA qa : qaList) {
            if (qa.getAnswer() != null) {
                context.append("Q: ").append(qa.getQuestion()).append("\n");
                context.append("A: ").append(qa.getAnswer()).append("\n\n");
            }
        }
        System.out.println(currQA.getAverageScore());
        // If number of questions answered equals totalQuestions, end interview
        if (qaList.size() >= session.getTotalQuestions()) {
            EndInterviewResponse endResponse = endInterview(sessionId);
            String reply = geminiService.answerCandidateQuestion(context.toString(),session.getResume(),session.getExperienceLevel(),session.getDomain(),request.getAnswer());
            String feedback = geminiService.feedbackOnInterview(context.toString(),session.getResume(),
                    session.getExperienceLevel(),session.getDomain());
            session.setFeedback(feedback);
            session.setScore(endResponse.getAverageScore());
            updateUserRating(endResponse.getAverageScore());
            interviewRepository.save(session);
            return new InterviewResponse(sessionId, reply,
                    "Interview completed.\n🎯 Final Score: " + endResponse.getAverageScore() +
                            "\n📋 Feedback: " + feedback);
        }



        // Determine the next question type
        String questionType = getQuestionTypeByNumber(questionNum + 1, session.getDomain());

        // Get the next question from Gemini
        String nextQuestion = geminiService.getNextQuestion(
                context.toString(),
                session.getResume(),
                request.getAnswer(),
                questionNum + 1,
                session.getExperienceLevel(),
                questionType,
                session.getDomain()
        );

        // Add the next question to the session
        InterviewSession.QA nextQA = new InterviewSession.QA();
        nextQA.setQuestion(nextQuestion);
        qaList.add(nextQA);

        session.setUpdatedAt(LocalDateTime.now());
        interviewRepository.save(session);

        return new InterviewResponse(sessionId, nextQuestion, scoreSummary);
    }

    public EndInterviewResponse endInterview(String sessionId) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Double> avgScores = session.getQaList().stream()
                .map(InterviewSession.QA::getAverageScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        double overallScore = avgScores.isEmpty()
                ? (7.5 + Math.random() * 2.5)
                : avgScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        session.setScore(overallScore);

        double avgScore = session.getScore() != null ? session.getScore() : (7.5 + Math.random() * 2.5);
        session.setScore(avgScore);
        session.setFeedback("Thank you, " + userRepository.findById(session.getUserId()).get().getUsername()
                + ", for your time. Your interview has now concluded." +
                " You can view your detailed scores and feedback for the session.");
        session.setUpdatedAt(LocalDateTime.now());

        interviewRepository.save(session);

        return new EndInterviewResponse(sessionId, avgScore, session.getFeedback());
    }

    private static final List<String> BASE_TYPES = List.of("CS_CORE", "PROJECT", "TECH_STACK");
    private List<String> categoryCycle = new ArrayList<>();
    private int categoryIndex = 0;
    private static final Random RANDOM = new Random();

    private String getQuestionTypeByNumber(int questionNum, String domain) {
        if (questionNum == 1) return "INTRO";

        // Refill and reshuffle once all categories are used
        if (categoryIndex >= categoryCycle.size()) {
            categoryCycle = new ArrayList<>(BASE_TYPES);
            Collections.shuffle(categoryCycle, RANDOM);
            categoryIndex = 0;
        }

        return categoryCycle.get(categoryIndex++);
    }




    private Map<String, Double> extractDetailedScores(String feedback) {
        Map<String, Double> scores = new HashMap<>();
        // Updated regex to allow optional dash and comment after the score
        Pattern pattern = Pattern.compile("(Confidence|Quality|Communication|Depth|Average(?:\\s*Score)?)\\s*:\\s*(\\d+(\\.\\d+)?)/10", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(feedback);
        while (matcher.find()) {
            String key = matcher.group(1).replaceAll("\\s*Score", "").trim(); // Handle "Average Score"
            Double value = Double.parseDouble(matcher.group(2));
            scores.put(capitalize(key), value);
        }
        return scores;
    }


    private String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    public InterviewSession getBySessionId(String sessionId) {
        Optional<InterviewSession> session = interviewRepository.findById(sessionId);
        return session.get();
    }

    public List<InterviewSession> getAllSessionsByUserId(String userId) {
        return interviewRepository.findByUserId(userId);
    }

    public Boolean deleteBySessionId(String sessionId, String userId) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this session");
        }
        interviewRepository.deleteById(sessionId);
        return true;

    }

    public String getResumeAnalysis(String resume) {
        return geminiService.getResumeAnalysis(resume);
    }

    public void updateUserRating(double sessionScore) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getCurrUser(username);
        int currentRating = user.getCurrentRating();
        int change = calculateRatingChange(sessionScore, currentRating);
        int newRating = currentRating + change;

        List<Integer> ratingHistory = user.getRatingHistory();
        if (ratingHistory == null) {
            ratingHistory = new ArrayList<>();
        }
        ratingHistory.add(newRating);

        // Update user
        user.setCurrentRating(newRating);
        user.setRatingHistory(ratingHistory);
        userRepository.save(user);
    }

    public int calculateRatingChange(double score, int currentRating) {
        int maxRatingGain = 50;
        double gainFactor = Math.max(0, (score - 6) / 4.0);  // 6–10 range
        double lossFactor = Math.min(1, (6 - score) / 6.0);  // 0–6 range

        double difficultyMultiplier = 1.0;
        if (currentRating >= 1500 && currentRating < 1800) difficultyMultiplier = 0.75;
        else if (currentRating >= 1800) difficultyMultiplier = 0.5;

        if (score > 6) {
            return (int) Math.round(gainFactor * maxRatingGain * difficultyMultiplier);
        } else {
            return -(int) Math.round(lossFactor * 25);  // Fixed penalty
        }
    }


}
