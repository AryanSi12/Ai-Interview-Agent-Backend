package com.Interview.AiAgent.Services;

import com.Interview.AiAgent.Models.User;
import com.Interview.AiAgent.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
