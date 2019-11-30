package me.ahmednur.twitterclone.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import me.ahmednur.twitterclone.models.Tweet;
import me.ahmednur.twitterclone.services.TweetService;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api")
public class TweetManagementController {
    @Autowired
    TweetService tweetService;

    @PostMapping("/compose")
    public ResponseEntity composeTweet(@RequestParam String content) {
        tweetService.createTweet(content);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/tweet/{parentTweetId:[0-9]+}/reply")
    public ResponseEntity replyToTweet(@PathVariable Long parentTweetId, @RequestParam String content) {
        try {
            tweetService.replyToTweet(parentTweetId, content);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/tweet/{postId:[0-9]+}")
    public ResponseEntity deleteTweet(@PathVariable Long postId) {
        try {
            tweetService.deleteTweet(postId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/tweet/{postId:[0-9]+}")
    public ResponseEntity<Tweet> getTweetById(@PathVariable Long postId){
        try {
            return new ResponseEntity<>(tweetService.getTweetById(postId), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/tweet/{parentTweetId:[0-9]+}/replies/{page:[0-9]+}")
    public ResponseEntity getReplies(@PathVariable Long parentTweetId, @PathVariable int page) {
        return new ResponseEntity<>(tweetService.getReplies(parentTweetId, page, 15), HttpStatus.OK);
    }

    @PostMapping("/tweet/{postId:[0-9]+}/like")
    public ResponseEntity likeTweet(@PathVariable Long postId) {
        try {
            tweetService.likeTweet(postId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/tweet/{postId:[0-9]+}/unlike")
    public ResponseEntity removeLikedTweet(@PathVariable Long postId) {
        try {
            tweetService.removeLikedTweet(postId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/tweet/{postId:[0-9]+}/retweet")
    public ResponseEntity retweet(@PathVariable Long postId) {
        try {
            tweetService.retweet(postId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/tweet/{retweetId:[0-9]+}/removeRetweet")
    public ResponseEntity removeRetweet(@PathVariable Long retweetId) {
        try {
            tweetService.removeRetweet(retweetId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            if(e.getMessage().equalsIgnoreCase("retweet not found")){
                return new ResponseEntity<>("retweet not found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("original tweet not found", HttpStatus.NOT_FOUND);
            }
        }
    }
}
