package twitterclone.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twitterclone.models.User;
import twitterclone.services.TweetService;
import twitterclone.services.UserService;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@CrossOrigin(origins = "http://localhost:4200")
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

    @PostMapping("/settings/tag")
    public ResponseEntity setTag(@RequestParam String tag) {
        try {
            userService.setUserTag(tag);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityExistsException e) {
            return new ResponseEntity<>("tag", HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/settings/displayname")
    public ResponseEntity setDisplayName(@RequestParam String displayName) {
        userService.setUserDisplayName(displayName);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/settings/bio")
    public ResponseEntity setBio(@RequestParam String bio) {
        userService.setUserBio(bio);
        return new ResponseEntity(HttpStatus.OK);
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
