package twitterclone.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import twitterclone.models.Tweet;

import java.util.List;

public interface TweetRepository extends PagingAndSortingRepository<Tweet, Long> {
    List<Tweet> findAllByTagOrderByDateCreatedDesc(String tag, Pageable pageable);
    List<Tweet> findAllByTagOrderByDateCreatedDesc(String tag);
    List<Tweet> findAllByParentIdAndContentNotNullOrderByLikesAsc(Long parentId, Pageable pageable);
    List<Tweet> findAllByTag(String tag);
    Tweet findById(int id);
}
