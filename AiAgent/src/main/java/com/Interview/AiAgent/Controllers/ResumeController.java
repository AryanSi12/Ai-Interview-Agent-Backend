package com.Interview.AiAgent.Controllers;

import com.Interview.AiAgent.Services.GeminiService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(@RequestParam("file") MultipartFile file) {
        try {
            Tika tika = new Tika();
            String resumeText = tika.parseToString(file.getInputStream());

            String responseJson = geminiService.extractResumeDetails(resumeText);
            return ResponseEntity.ok(responseJson);

        } catch (IOException | TikaException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to parse resume: " + e.getMessage());
        }
    }
}
