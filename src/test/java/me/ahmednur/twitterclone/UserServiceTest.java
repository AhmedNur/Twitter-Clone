package me.ahmednur.twitterclone;

import lombok.val;
import me.ahmednur.twitterclone.models.Tweet;
import me.ahmednur.twitterclone.repositories.TweetRepository;
import me.ahmednur.twitterclone.repositories.UserRepository;
import me.ahmednur.twitterclone.services.UserService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import me.ahmednur.twitterclone.models.User;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepo;
    @Mock
    TweetRepository tweetRepo;
    @InjectMocks
    UserService userService;

    public UserServiceTest(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenAuthenticatedUser_whenLoadOwnProfile_thenReturnPrincipalWithoutPassword() {
        val expected = new User();
        expected.setUsername("user");
        expected.setId(0L);
        expected.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expected, "password"));
        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(expected));
        User actual = userService.loadOwnProfile();
        Assertions.assertThat(actual.getUsername()).isEqualTo(expected.getUsername());
        Assertions.assertThat(actual.getId()).isEqualTo(expected.getId());
        Assertions.assertThat(actual.getPassword()).isBlank();
    }

    @Test
    public void givenAnonymousUser_whenLoadOwnProfile_thenReturnUserWithUsernameAnonymousUser() {
        val expected = new User();
        expected.setUsername("anonymoususer");

        val anonymousAuthorities = new ArrayList<SimpleGrantedAuthority>();
        anonymousAuthorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymous", anonymousAuthorities));
        User actual = userService.loadOwnProfile();
        Assertions.assertThat(actual.getUsername()).isEqualTo(expected.getUsername());
    }

    @Test
    public void givenValidTag_whenLoadProfile_thenReturnUserWithoutPassword(){
        val expected = new User();
        expected.setUsername("user");
        expected.setTag("Tag");
        expected.setPassword("password");

        Mockito.when(userRepo.findByTag("Tag")).thenReturn(expected);
        val actual = userService.loadProfile("Tag");

        Assertions.assertThat(actual.getUsername()).isEqualTo(expected.getUsername());
        Assertions.assertThat(actual.getTag()).isEqualTo(expected.getTag());
        Assertions.assertThat(actual.getPassword()).isBlank();
    }

    @Test(expected = EntityNotFoundException.class)
    public void givenInvalidTag_whenLoadProfile_thenThrowEntityNotFoundException() {
        userService.loadProfile("invalid");
    }

    @Test
    public void givenValidTag_whenFollowUser_thenAddUserIdToCurrentUserFollowedListAndCurrentUserIdToUserFollowersList() {
        val currentUser = new User();
        currentUser.setUsername("user1");
        currentUser.setTag("user1");
        currentUser.setId(0L);
        currentUser.setFollowed(new ArrayList<>());
        currentUser.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));
        val targetUser = new User();
        targetUser.setTag("user2");
        targetUser.setId(1L);
        targetUser.setFollowers(new ArrayList<>());

        Mockito.when(userRepo.findByTag("user2")).thenReturn(targetUser);
        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));

        userService.followUser("user2");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepo, times(2)).save(userCaptor.capture());

        val results = userCaptor.getAllValues();
        Assertions.assertThat(results.get(0).getFollowed().contains(1L)).isTrue();
        Assertions.assertThat(results.get(1).getFollowers().contains(0L)).isTrue();
    }

    @Test(expected = EntityNotFoundException.class)
    public void givenInvalidTag_whenFollowUser_thenThrowEntityNotFoundException() {
        userService.followUser("invalid");
    }

    @Test
    public void givenValidTag_whenUnfollowUser_thenRemoveUserIdFromCurrentUserFollowedListAndRemoveCurrentUserIdFromTargetFollowersList() {
        val currentUser = new User();
        currentUser.setUsername("user1");
        currentUser.setTag("user1");
        currentUser.setId(0L);
        val followed = new ArrayList<Long>();
        followed.add(1L);
        currentUser.setFollowed(followed);
        currentUser.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));
        val targetUser = new User();
        targetUser.setTag("user2");
        targetUser.setId(1L);
        val followers = new ArrayList<Long>();
        followers.add(0L);
        targetUser.setFollowers(followers);

        Mockito.when(userRepo.findByTag("user2")).thenReturn(targetUser);
        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));

        userService.unfollowUser("user2");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepo, times(2)).save(userCaptor.capture());

        val results = userCaptor.getAllValues();
        Assertions.assertThat(results.get(0).getFollowed().contains(1L)).isFalse();
        Assertions.assertThat(results.get(1).getFollowers().contains(0L)).isFalse();
    }

    @Test(expected = EntityNotFoundException.class)
    public void givenInvalidTag_whenUnfollowUser_thenThrowEntityNotFoundException() {
        userService.unfollowUser("invalid");
    }

    @Test
    public void givenTagNotEqualToCurrentTagAndNotTaken_whenUpdateProfile_thenChangeTagFieldInCurrentUserObjectAndAllCurrentUserTweets() throws AuthenticationException {
        val currentUser = new User();
        currentUser.setUsername("user");
        currentUser.setTag("user");
        currentUser.setBio("bio");
        currentUser.setDisplayName("displayName");
        currentUser.setId(0L);
        currentUser.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));
        val tweets = new ArrayList<Tweet>();
        for(int i = 0; i < 5; i++) {
            Tweet t = new Tweet();
            t.setTag("user");
            tweets.add(t);
        }

        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));
        Mockito.when(tweetRepo.findAllByTag("user")).thenReturn(tweets);

        userService.updateProfile("changedTag", "displayName", "bio");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
        Mockito.verify(userRepo).save(userCaptor.capture());
        Mockito.verify(tweetRepo, times(5)).save(tweetCaptor.capture());

        val updatedUser = userCaptor.getValue();
        val updatedTweets = tweetCaptor.getAllValues();

        Assertions.assertThat(updatedUser.getTag()).isEqualTo("changedTag");
        for(Tweet t : updatedTweets) {
            Assertions.assertThat(t.getTag()).isEqualTo("changedTag");
        }
    }

    @Test(expected = EntityExistsException.class)
    public void givenTakenTag_whenUpdateProfile_thenThrowEntityExistsException() throws AuthenticationException {
        val currentUser = new User();
        currentUser.setUsername("user");
        currentUser.setTag("user");
        currentUser.setId(0L);
        currentUser.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));

        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));
        Mockito.when(userRepo.findByTag("taken")).thenReturn(new User());

        userService.updateProfile("taken", "name", "bio");
    }

    @Test
    public void givenCurrentTag_whenUpdateProfile_thenDoNothing() throws AuthenticationException {
        val currentUser = new User();
        currentUser.setUsername("user");
        currentUser.setTag("user");
        currentUser.setId(0L);
        currentUser.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));

        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));

        userService.updateProfile("user", "name", "bio");
    }

    @Test
    public void givenNewDisplayName_whenUpdateProfile_thenChangeDisplayNameFieldInCurrentUserAndAllCurrentUserTweets() throws AuthenticationException {
    val currentUser = new User();
    currentUser.setUsername("user");
    currentUser.setTag("user");
    currentUser.setDisplayName("name");
    currentUser.setId(0L);
    currentUser.setPassword("password");
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));
    val tweets = new ArrayList<Tweet>();
    for(int i = 0; i < 5; i++) {
        Tweet t = new Tweet();
        t.setTag("user");
        t.setDisplayName("name");
        tweets.add(t);
    }

    Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));
    Mockito.when(tweetRepo.findAllByTag("user")).thenReturn(tweets);

    userService.updateProfile("user", "changed", "bio");
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    ArgumentCaptor<Tweet> tweetCaptor = ArgumentCaptor.forClass(Tweet.class);
    Mockito.verify(userRepo).save(userCaptor.capture());
    Mockito.verify(tweetRepo, times(5)).save(tweetCaptor.capture());

    val updatedUser = userCaptor.getValue();
    val updatedTweets = tweetCaptor.getAllValues();

    Assertions.assertThat(updatedUser.getDisplayName()).isEqualTo("changed");
    for(Tweet t : updatedTweets) {
        Assertions.assertThat(t.getDisplayName()).isEqualTo("changed");
    }
}

    @Test
    public void givenNewBio_whenUpdateProfile_thenChangeCurrentUserBio() throws AuthenticationException {
        val currentUser = new User();
        currentUser.setUsername("user");
        currentUser.setTag("user");
        currentUser.setId(0L);
        currentUser.setPassword("password");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, "password"));

        Mockito.when(userRepo.findById(0L)).thenReturn(Optional.of(currentUser));

        userService.updateProfile("user", "name", "bio");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepo).save(userCaptor.capture());

        val results = userCaptor.getValue();
        Assertions.assertThat(results.getBio()).isEqualTo("bio");
    }
}
