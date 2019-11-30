package me.ahmednur.twitterclone.controllers;

import me.ahmednur.twitterclone.services.RegistrationService;
import me.ahmednur.twitterclone.util.UsernameExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import me.ahmednur.twitterclone.models.User;
import me.ahmednur.twitterclone.services.UserService;

import javax.persistence.EntityExistsException;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    @Autowired
    RegistrationService registrationService;
    @Autowired
    UserService userService;

    @GetMapping("/me")
    public User getMe(){
        return userService.loadOwnProfile();
    }

    @PostMapping("/register")
    public ResponseEntity registerAccount(@RequestParam String username, @RequestParam String tag, @RequestParam String displayName, @RequestParam String password){
        try {
            registrationService.registerAccount(username, tag, displayName, password);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UsernameExistsException e) {
            return new ResponseEntity<>("username", HttpStatus.CONFLICT);
        } catch(EntityExistsException e) {
            return new ResponseEntity<>("tag", HttpStatus.CONFLICT);
        }
    }
}
