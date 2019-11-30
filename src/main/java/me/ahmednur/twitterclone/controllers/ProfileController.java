package me.ahmednur.twitterclone.controllers;

import me.ahmednur.twitterclone.services.TweetService;
import me.ahmednur.twitterclone.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import me.ahmednur.twitterclone.models.User;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api")
public class ProfileController {
    @Autowired
    UserService userService;
    @Autowired
    TweetService tweetService;

    @GetMapping("/profile/{tag:[a-zA-Z0-9-_]+}")
    public ResponseEntity<User> getProfile(@PathVariable String tag) {
        try {
            return new ResponseEntity<User>(userService.loadProfile(tag), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/profile/{tag:[a-zA-Z0-9-_]+}/{page:[0-9]+}")
    public ResponseEntity getTweetsByAuthor(@PathVariable String tag, @PathVariable int page) {
        return new ResponseEntity<>(tweetService.getTweetsByAuthor(tag, page, 15), HttpStatus.OK);
    }

    @PostMapping("/me/update")
    public ResponseEntity updateProfile(@RequestParam String tag, @RequestParam String displayName, @RequestParam String bio) {
        try {
            userService.updateProfile(tag, displayName, bio);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityExistsException ex) {
            return new ResponseEntity<>("tag", HttpStatus.CONFLICT);
        } catch (AuthenticationException e) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

    }

    @PostMapping("/profile/{tag:[a-zA-Z0-9-_]+}/follow")
    public ResponseEntity followUser(@PathVariable String tag) {
        try {
            userService.followUser(tag);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/profile/{tag:[a-zA-Z0-9-_]+}/unfollow")
    public ResponseEntity unfollowUser(@PathVariable String tag) {
        try {
            userService.unfollowUser(tag);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
