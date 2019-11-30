package me.ahmednur.twitterclone.controllers;

import me.ahmednur.twitterclone.models.Tweet;
import me.ahmednur.twitterclone.services.TweetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

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
