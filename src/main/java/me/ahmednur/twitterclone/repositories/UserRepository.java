package me.ahmednur.twitterclone.repositories;

import me.ahmednur.twitterclone.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);

    User findByTag(String tag);
}
