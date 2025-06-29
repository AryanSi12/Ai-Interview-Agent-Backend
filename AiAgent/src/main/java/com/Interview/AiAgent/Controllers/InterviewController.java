package com.Interview.AiAgent.Controllers;

import com.Interview.AiAgent.DTO.InterviewResponse;
import com.Interview.AiAgent.DTO.StartInterviewRequest;
import com.Interview.AiAgent.DTO.SubmitAnswerRequest;
import com.Interview.AiAgent.Models.InterviewSession;
import com.Interview.AiAgent.Models.User;
import com.Interview.AiAgent.Services.InterviewService;
import com.Interview.AiAgent.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Autowired
    ResumeController resumeController;

    @Autowired
    InterviewService interviewService;

    @Autowired
    UserService userService;
    @PostMapping(value = "/startInterview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> startInterview(
            @RequestParam("experienceLevel") String experienceLevel,
            @RequestParam("domain") String domain,
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam(name = "questions", required = false, defaultValue = "5") int qno

    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getCurrUser(username);
            String userId = user.getUserId();
            String resumeText = String.valueOf(resumeController.parseResume(resumeFile));
            StartInterviewRequest request = new StartInterviewRequest();
            request.setUserId(userId);
            request.setDomain(domain);
            request.setExperienceLevel(experienceLevel);
            request.setResume(resumeText);
            request.setTotalQuestions(qno);


            InterviewResponse response = interviewService.startInterview(request);

            return new ResponseEntity<>(response,HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error occurred while starting the interview", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/handleAnswer/{sessionId}")
    public ResponseEntity<?> handleAnswer(@PathVariable String sessionId,
                                          @RequestBody SubmitAnswerRequest request){
        try {
            InterviewResponse response = interviewService.handleSubmit(sessionId,request);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error while continuing dynamic interview",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getSessionDetailById/{sessionId}")
    public ResponseEntity<?> getBySessionId(@PathVariable String sessionId){
        try{
            InterviewSession session=interviewService.getBySessionId(sessionId);
            return new ResponseEntity<>(session,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching the session",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getAllSessionsByUser")
    public ResponseEntity<?> getAllSessionsByUser(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getCurrUser(username);
            String userId = user.getUserId();
            List<InterviewSession> sessions = interviewService.getAllSessionsByUserId(userId);
            return new ResponseEntity<>(sessions,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unable to fetch the user session(check if user is Authenticated)",HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/deleteSessionById/{sessionId}")
    public ResponseEntity<?> deleteSessionById(@PathVariable String sessionId){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getCurrUser(username);
            String userId = user.getUserId();
            Boolean isDeleted = interviewService.deleteBySessionId(sessionId,userId);
            return new ResponseEntity<>(isDeleted,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Unable to delete the session",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/getResumeAnalysis")
    public ResponseEntity<?> getResumeAnalysis(@RequestParam("file") MultipartFile file){
        try{
            String resumeText = String.valueOf(resumeController.parseResume(file));
            String resumeAnalysis = interviewService.getResumeAnalysis(resumeText);
            return new ResponseEntity<>(resumeAnalysis,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching resume analysis",HttpStatus.BAD_REQUEST);
        }
    }
}
