package com.Interview.AiAgent.Models;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
public class User {
    @Id
    private String userId;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String userImage;

    private String password;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


}
