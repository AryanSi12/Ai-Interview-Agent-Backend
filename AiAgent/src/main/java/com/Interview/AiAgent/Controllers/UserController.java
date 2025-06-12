package com.Interview.AiAgent.Controllers;

import com.Interview.AiAgent.DTO.UpdateUserRequest;
import com.Interview.AiAgent.Models.User;
import com.Interview.AiAgent.Services.UserService;
import com.Interview.AiAgent.Utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.Interview.AiAgent.Controllers.PublicController.passwordEncoder;

@RestController
@RequestMapping("/User")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/getUserDetails")
    public ResponseEntity<?> getUserDetails(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getCurrUser(username);
            if (user != null)return new ResponseEntity<>(user, HttpStatus.OK);
            return new ResponseEntity<>("Faced problem fetching the user",HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Can't fetch the userDetails",HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/Logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try{
            Cookie cookie = new Cookie("jwtToken", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/"); // Match the path of the original cookie
            cookie.setMaxAge(0); // Delete the cookie immediately

            response.addCookie(cookie);

            return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to logout",HttpStatus.FORBIDDEN);
        }

    }

    @GetMapping("/getCurrUser")
    public ResponseEntity<?> getCurrUser(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getCurrUser(username);
            return new ResponseEntity<>(user,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unable to fetch the current user",HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateUserDetails")
    public ResponseEntity<?> updateUserDetails(@RequestBody UpdateUserRequest updatedDetails, HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            User user = userService.getUserByName(currentUsername);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
            }

            boolean usernameUpdated = false;

            // ✅ Check for password update
            if (updatedDetails.getPassword() != null && !updatedDetails.getPassword().isEmpty()) {
                if (updatedDetails.getCurrentPassword() == null ||
                        !passwordEncoder().matches(updatedDetails.getCurrentPassword(), user.getPassword())) {
                    return new ResponseEntity<>("Incorrect current password", HttpStatus.UNAUTHORIZED);
                }
                user.setPassword(passwordEncoder().encode(updatedDetails.getPassword()));
            }

            // ✅ Check for username update
            if (updatedDetails.getUsername() != null &&
                    !updatedDetails.getUsername().isEmpty() &&
                    !updatedDetails.getUsername().equals(user.getUsername())) {
                user.setUsername(updatedDetails.getUsername());
                usernameUpdated = true;
            }

            user.setUpdatedAt(LocalDateTime.now());
            userService.saveUser(user);

            // ✅ Regenerate token ONLY if username changed
            if (usernameUpdated) {
                String newToken = jwtUtil.generateToken(user.getUsername());
                ResponseCookie cookie = ResponseCookie.from("jwtToken", newToken)
                        .httpOnly(true)
                        .path("/")
                        .maxAge(24 * 60 * 60)
                        .build();
                response.setHeader("Set-Cookie", cookie.toString());
            }

            return new ResponseEntity<>(user, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unable to update the user", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/deleteUserById/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable String userId,HttpServletResponse response){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            System.out.println(username);
            if (!userService.deleteByUserId(userId,username))return new ResponseEntity<>("Forbidden to delete someone else's id",HttpStatus.FORBIDDEN);
            Cookie cookie = new Cookie("jwtToken", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/"); // Match the path of the original cookie
            cookie.setMaxAge(0); // Delete the cookie immediately

            response.addCookie(cookie);
            return new ResponseEntity<>(userId,HttpStatus.OK);
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
