package me.ahmednur.twitterclone;

import lombok.val;
import me.ahmednur.twitterclone.models.Tweet;
import me.ahmednur.twitterclone.models.User;
import me.ahmednur.twitterclone.repositories.TweetRepository;
import me.ahmednur.twitterclone.repositories.UserRepository;
import me.ahmednur.twitterclone.services.TweetService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class TweetServiceTest {
    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TweetService tweetService;

    public TweetServiceTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenValidId_whenGetTweetById_thenReturnValidTweet() {
        val id = 384137491L;
        val tweet = Optional.of(new Tweet());

        Mockito.when(tweetRepository.findById(384137491L)).thenReturn(tweet);

        val actual = this.tweetService.getTweetById(id);
        Assertions.assertThat(actual).isEqualTo(tweet.get());

    }

    @Test(expected = EntityNotFoundException.class)
    public void givenInvalidId_whenGetTweetById_thenThrowEntityNotFoundException() {
        val id = 3L;

        val actual = this.tweetService.getTweetById(id);
    }

    @Test
    public void givenAuthenticatedUser_whenGetFollowedAuthorsTweets_thenReturnListOfFollowedAuthorsTweetsSortedByDateDesc() {
        val currentUser = new User();
        currentUser.setUsername("user");
        currentUser.setTag("user");
        currentUser.setId(0L);
        currentUser.setPassword("password");
        currentUser.setFollowed(new ArrayList<Long>());
        currentUser.getFollowed().add(1L);
        currentUser.getFollowed().add(2L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));
        val user2 = new User();
        user2.setId(1L);
        user2.setTag("user2");
        val user3 = new User();
        user3.setId(2L);
        user3.setTag("user3");
        val user2Tweets = new ArrayList<Tweet>();
        val user3Tweets = new ArrayList<Tweet>();
        for(int i = 0; i < 10; i++) {
            if(i % 2 == 0) {
                Tweet t = new Tweet();
                t.setTag("user2");
                t.setContent("content");
                t.setDateCreated(Integer.toUnsignedLong(i));
                user2Tweets.add(t);
            } else {
                Tweet t = new Tweet();
                t.setTag("user3");
                t.setContent("content");
                t.setDateCreated(Integer.toUnsignedLong(i));
                user3Tweets.add(t);
            }
        }
        val expected = new ArrayList<Tweet>();
        expected.addAll(user2Tweets);
        expected.addAll(user3Tweets);
        expected.sort(Comparator.comparing(Tweet::getDateCreated).reversed());

        Mockito.when(userRepository.findById(0L)).thenReturn(Optional.of(currentUser));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user2));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user3));
        Mockito.when(tweetRepository.findAllByTagOrderByDateCreatedDesc("user2")).thenReturn(user2Tweets);
        Mockito.when(tweetRepository.findAllByTagOrderByDateCreatedDesc("user3")).thenReturn(user3Tweets);

        val actual = tweetService.getFollowedAuthorsTweets(0);
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
