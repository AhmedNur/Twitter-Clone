package twitterclone.repositories;

import org.springframework.data.repository.CrudRepository;
import twitterclone.models.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
    User findByTag(String tag);
}
