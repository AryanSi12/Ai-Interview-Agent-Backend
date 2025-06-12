package com.Interview.AiAgent.Services;

import com.Interview.AiAgent.Models.User;
import com.Interview.AiAgent.Repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User getCurrUser(String username) {
        return userRepository.findByusername(username);
    }

    public User getUserByName(String username) {
        return userRepository.findByusername(username);
    }


    public boolean deleteByUserId(String Id, String username) {
        User user = userRepository.findByusername(username);
        System.out.println(user.getUserId()+" "+Id);
        if(!user.getUserId().equals(Id)){
            return false;
        }
        userRepository.deleteById(Id);

        return true;
    }

    public User getUserById(String userId) {
        Optional<User> userOptional =  userRepository.findById(userId);
        return userOptional.get();
    }

}
