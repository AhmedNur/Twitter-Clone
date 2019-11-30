package me.ahmednur.twitterclone.services;

import lombok.extern.slf4j.Slf4j;
import me.ahmednur.twitterclone.models.Tweet;
import me.ahmednur.twitterclone.models.User;
import me.ahmednur.twitterclone.repositories.TweetRepository;
import me.ahmednur.twitterclone.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserRepository userRepo;
    @Autowired
    TweetRepository tweetRepo;

    public User loadOwnProfile() {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
            currentUser.setPassword("");
            return currentUser;
        }
        User anon = new User();
        anon.setUsername("anonymoususer");
        return anon;
    }

    public User loadProfile(String tag) {
        User retrievedUser = userRepo.findByTag(tag);
        if (retrievedUser == null) {
            throw new EntityNotFoundException("Not Found.");
        }
        retrievedUser.setPassword("");
        return retrievedUser;
    }

    @Transactional
    public void followUser(String tag) {
        User followedUser = userRepo.findByTag(tag);
        if (followedUser == null) {
            throw new EntityNotFoundException();
        }
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        currentUser.getFollowed().add(followedUser.getId());
        followedUser.getFollowers().add(currentUser.getId());
        userRepo.save(currentUser);
        userRepo.save(followedUser);
    }

    @Transactional
    public void unfollowUser(String tag) {
        User followedUser = userRepo.findByTag(tag);
        if (followedUser == null) {
            throw new EntityNotFoundException();
        }
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if (currentUser.getFollowed().contains(followedUser.getId())) {
            currentUser.getFollowed().remove(followedUser.getId());
            followedUser.getFollowers().remove(currentUser.getId());
            userRepo.save(currentUser);
            userRepo.save(followedUser);
        }
    }

    @Transactional
    public void updateProfile(String tag, String displayName, String bio) throws AuthenticationException {
        User currentUser = userRepo.findById((((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())).orElseThrow(AuthenticationException::new);
        if (!tag.equals(currentUser.getTag())) {
            if (userRepo.findByTag(tag) != null) {
                throw new EntityExistsException();
            }
            for (Tweet t : tweetRepo.findAllByTag(currentUser.getTag())) {
                t.setTag(tag);
                tweetRepo.save(t);
            }
            currentUser.setTag(tag);
        }
        if (!displayName.equals(currentUser.getDisplayName())) {
            for (Tweet t : tweetRepo.findAllByTag(currentUser.getTag())) {
                t.setDisplayName(displayName);
                tweetRepo.save(t);
            }
            currentUser.setDisplayName(displayName);
        }
        if (!bio.equals(currentUser.getBio())) {
            currentUser.setBio(bio);
        }
        userRepo.save(currentUser);
    }
}
