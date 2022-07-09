package twitterclone.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitterclone.models.Tweet;
import twitterclone.models.User;
import twitterclone.repositories.TweetRepository;
import twitterclone.repositories.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class TweetService {
    @Autowired
    TweetRepository tweetRepo;
    @Autowired
    UserRepository userRepo;

    public Tweet getTweetById(Long id) {
        Tweet retrievedTweet = tweetRepo.findById(id).get();
        if(retrievedTweet == null) {
            throw new EntityNotFoundException();
        }
        return retrievedTweet;
    }

    public ArrayList<Tweet> getReplies(Long parentTweetId, int page, int numOfElements) {
        Pageable pageable = PageRequest.of(page, numOfElements);
        ArrayList<Tweet> replies = new ArrayList<>(tweetRepo.findAllByParentIdAndContentNotNullOrderByLikesAsc(parentTweetId, pageable));
        return replies;
    }

    public ArrayList<Tweet> getTweetsByAuthor(String tag, int page, int numOfElements) {
        Pageable pageable = PageRequest.of(page, numOfElements);
        return new ArrayList<>(tweetRepo.findAllByTagOrderByDateCreatedDesc(tag, pageable));
    }

    public ArrayList<Tweet> getFollowedAuthorsTweets(int page) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        ArrayList<Tweet> retrievedTweets = new ArrayList<>();
        for(Long userId : currentUser.getFollowed()) {
            retrievedTweets.addAll(new ArrayList<>(tweetRepo.findAllByTagOrderByDateCreatedDesc(userRepo.findById(userId).get().getTag())));
        }
        retrievedTweets.sort(Comparator.comparing(Tweet::getDateCreated).reversed());
        if(retrievedTweets.size() > ((page*15) + 15)) {
            return new ArrayList<>(retrievedTweets.subList(page * 15, (page * 15) + 15));
        } else if(retrievedTweets.size() > page*15) {
            return new ArrayList<>(retrievedTweets.subList(page*15, (retrievedTweets.size())));
        } else {
            return new ArrayList<>();
        }
    }

    @Transactional
    public long createTweet(String content) {
        System.out.println(content);
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        Tweet newTweet = new Tweet();
        newTweet.setContent(content);
        newTweet.setDateCreated(System.currentTimeMillis());
        newTweet.setDisplayName(currentUser.getDisplayName());
        newTweet.setTag(currentUser.getTag());
        newTweet.setLikes(0);
        newTweet.setNumOfReplies(0);
        newTweet.setRetweets(0);
        long newTweetId = tweetRepo.save(newTweet).getId();
        currentUser.getTweets().add(newTweetId);
        userRepo.save(currentUser);
        return newTweetId;
    }

    public void deleteTweet(Long id) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(!tweetRepo.existsById(id)){
            throw new EntityNotFoundException();
        } else if(!currentUser.getTag().equals(tweetRepo.findById(id).get().getTag())){
            throw new AccessDeniedException("You can only delete your own tweets.");
        }
        currentUser.getTweets().remove(id);
        tweetRepo.deleteById(id);
        userRepo.save(currentUser);
    }

    @Transactional
    public long replyToTweet(Long parentTweetId, String content) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        Tweet parentTweet = tweetRepo.findById(parentTweetId).get();
        if(parentTweet == null) {
            throw new EntityNotFoundException();
        }
        Tweet reply = new Tweet();
        reply.setLikes(0);
        reply.setNumOfReplies(0);
        reply.setRetweets(0);
        reply.setTag(currentUser.getTag());
        reply.setDisplayName(currentUser.getDisplayName());
        reply.setDateCreated(System.currentTimeMillis());
        reply.setContent(content);
        reply.setParentId(parentTweetId);
        long replyId = tweetRepo.save(reply).getId();
        parentTweet.getReplies().add(replyId);
        parentTweet.setNumOfReplies(parentTweet.getNumOfReplies() + 1);
        return replyId;
    }

    @Transactional
    public void likeTweet(Long id) {
        Tweet retrievedTweet = tweetRepo.findById(id).get();
        if (retrievedTweet == null) {
            throw new EntityNotFoundException();
        }
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(!(currentUser.getLikedTweets().contains(id))) {
            currentUser.getLikedTweets().add(id);
            userRepo.save(currentUser);
            retrievedTweet.setLikes(retrievedTweet.getLikes() + 1);
            tweetRepo.save(retrievedTweet);
        }
    }

    @Transactional
    public void removeLikedTweet(Long id) {
        Tweet retrievedTweet = tweetRepo.findById(id).get();
        if (retrievedTweet == null) {
            throw new EntityNotFoundException();
        }
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(currentUser.getLikedTweets().contains(id)) {
            currentUser.getLikedTweets().remove(id);
            userRepo.save(currentUser);
            retrievedTweet.setLikes(retrievedTweet.getLikes() - 1);
            tweetRepo.save(retrievedTweet);
        }
    }

    @Transactional
    public void retweet(Long id) {
        Tweet originalTweet = tweetRepo.findById(id).get();
        if(originalTweet == null) {
            throw new EntityNotFoundException();
        }
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(!(currentUser.getRetweets().contains(id))) {
            Tweet retweet = new Tweet();
            retweet.setParentId(id);
            retweet.setDateCreated(System.currentTimeMillis());
            retweet.setTag(currentUser.getTag());
            Long retweetId = tweetRepo.save(retweet).getId();
            currentUser.getTweets().add(retweetId);
            currentUser.getRetweets().add(id);
            currentUser.getParentIdToRetweetId().put(id, retweetId);
            userRepo.save(currentUser);
            originalTweet.setRetweets(originalTweet.getRetweets() + 1);
            tweetRepo.save(originalTweet);
        }
    }

    @Transactional
    public void removeRetweet(Long retweetId) {
        User currentUser = userRepo.findById(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId()).get();
        if(currentUser.getTweets().contains(retweetId)) {
            Tweet retweet = tweetRepo.findById(retweetId).get();
            if(retweet == null) {
                throw new EntityNotFoundException("retweet not found");
            }
            Tweet originalTweet = tweetRepo.findById(retweet.getParentId()).get();
            if(originalTweet == null){
                throw new EntityNotFoundException("original tweet not found");
            }
            originalTweet.setRetweets(originalTweet.getRetweets() - 1);
            currentUser.getRetweets().remove(originalTweet.getId());
            currentUser.getTweets().remove(retweetId);
            currentUser.getParentIdToRetweetId().remove(originalTweet.getId());
            tweetRepo.deleteById(retweetId);
            tweetRepo.save(originalTweet);
            userRepo.save(currentUser);
        }
    }
}
