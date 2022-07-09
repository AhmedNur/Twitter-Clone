package twitterclone.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twitterclone.models.Tweet;
import twitterclone.services.TweetService;

import java.util.ArrayList;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class HomeTimelineController {
    @Autowired
    TweetService tweetService;

    @GetMapping("/{page:[0-9]+}")
    public ResponseEntity<ArrayList<Tweet>> getHomeTimeline(@PathVariable int page){
        return new ResponseEntity<>(tweetService.getFollowedAuthorsTweets(page), HttpStatus.OK);
    }
}
