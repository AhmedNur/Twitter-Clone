package twitterclone.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitterclone.models.Tweet;
import twitterclone.models.User;
import twitterclone.repositories.TweetRepository;
import twitterclone.repositories.UserRepository;

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
        if(!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)){
            log.debug(SecurityContextHolder.getContext().getAuthentication().isAuthenticated() + " " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString() );
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
        if(retrievedUser == null){
            throw new EntityNotFoundException("Not Found.");
        }
        retrievedUser.setPassword("");
        return retrievedUser;
    }

    @Transactional
    public void followUser(String tag) {
        User followedUser = userRepo.findByTag(tag);
        if(followedUser == null) {
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
        if(followedUser == null) {
            throw new EntityNotFoundException();
        }
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(currentUser.getFollowed().contains(followedUser.getId())) {
            currentUser.getFollowed().remove(followedUser.getId());
            followedUser.getFollowers().remove(currentUser.getId());
            userRepo.save(currentUser);
            userRepo.save(followedUser);
        }
    }

    public void setUserTag(String tag) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(currentUser.getTag().equalsIgnoreCase(tag)){
            return;
        } else if (userRepo.findByTag(tag) != null) {
            throw new EntityExistsException();
        }
        for(Tweet t : tweetRepo.findAllByTag(currentUser.getTag())) {
            t.setTag(tag);
        }
        currentUser.setTag(tag);
        userRepo.save(currentUser);
    }

    public void setUserDisplayName(String displayName) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        for(Tweet t : tweetRepo.findAllByTag(currentUser.getTag())) {
            t.setDisplayName(displayName);
        }
        currentUser.setDisplayName(displayName);
        userRepo.save(currentUser);
    }

    public void setUserBio(String bio) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        currentUser.setBio(bio);
        userRepo.save(currentUser);
    }
}
