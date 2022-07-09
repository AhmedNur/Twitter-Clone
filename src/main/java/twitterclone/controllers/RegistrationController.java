package twitterclone.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twitterclone.models.User;
import twitterclone.services.RegistrationService;
import twitterclone.services.UserService;
import twitterclone.util.UsernameExistsException;

import javax.persistence.EntityExistsException;

@CrossOrigin(origins = "http://localhost:4200")
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
